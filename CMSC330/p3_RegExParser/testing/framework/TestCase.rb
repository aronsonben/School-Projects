require_relative "TestUtils.rb"
require_relative "TestConfig.rb"
require "fileutils"
require "logger"
require "open3"

class TestCase

    # Create a logger that really functions more like a discretionary print system
    @@LOGGER = Logger.new(STDOUT)
    @@LOGGER.level = Logger::INFO
    @@LOGGER.formatter = proc { |severity, datetime, progname, msg|
        "#{msg}\n"
    }

    def initialize(test_dir)
        # Ensure that the test directory exists
        unless File.directory?(test_dir)
            @@LOGGER.fatal{"Test directory (#{test_dir}) was not found"}
            exit 2
        else
            @test_dir = File.expand_path(test_dir)
            @@LOGGER.debug{"\tTest directory found: '#{@test_dir}'"}
        end

        @@LOGGER.debug{"Test Home: '#{@test_dir}'"}

        # First thing first, parse flags
        @@LOGGER.debug{"Parsing flags..."}

        # Set up default values for things affected by flags
        @leavenotrace = false

        ARGV.each{|flag|
            if (flag == "-v")
                @@LOGGER.level = Logger::DEBUG
                @@LOGGER.info{"> '-v' - verbose mode (shows more detailed output)"}
            elsif (flag == "-c")
                @@LOGGER.info{"> '-c' - cleanup mode (audo-delete output files)"}
                @leavenotrace = true
            end
        }
        @@LOGGER.debug{"Flag parse complete!"}

        @@LOGGER.debug{"Loading config file..."}
        @test_config = TestConfig.new(get_test_config_filename)
        @@LOGGER.debug{"Config successfully loaded!"}
        @@LOGGER.info{"Running test: #{@test_config.name}"}

        # Store name and type
        @test_name = @test_config.name
        @@LOGGER.debug{"\tNumber of test modules: #{@test_config.modules.size}"}

        ##############################################
        # Validate that all necessary files are here #
        ##############################################

        @@LOGGER.debug{"Validating test structure..."}

        # Ensure that every module has its output file
        @test_config.modules.each{|mod, command|

            @@LOGGER.debug{"\tValidating files for #{mod}... "}
            unless File.exists?(get_expected_output_filename mod)
                puts "it was called #{get_expected_output_filename mod}"
                integrity_exit "Missing output file for #{mod}" 
            end

            @@LOGGER.debug{"\t\t#{mod} is valid!"}
        }

        ########################
        # Do resource mappings #
        ########################

        # By default, no resources are mapped
        @filename_map = {}

        # Get the filenames from the config
        @standard_resources = @test_config.resources
        @local_resources = @test_config.local_resources

        # Make sure neither is nil
        @standard_resources = [] if @standard_resources == nil
        @local_resources = [] if @local_resources == nil

        # Go over each resource, validate, and map
        (@standard_resources + @local_resources).each{|dependency|
            @@LOGGER.debug{"\tSearching for dependency '#{dependency}'"}

            # Place in the current directory as a default
            @filename_map[dependency] = File.join(@test_dir, dependency)

            # These are the possible locations the files could be found
            # Highest precedence to lowest precedence
            in_local = @filename_map[dependency]
            in_common = File.join(@test_dir, "..", "common", dependency)
            in_home = File.join(@test_dir, "..", "..", dependency)
            in_test_home = File.join(@test_dir, "..", dependency)

            if (File.exists? in_local)
                @@LOGGER.debug{"\t\tFound '#{dependency}' in module directory"}
            elsif (File.exists? in_common)
                @filename_map[dependency] = in_common
                @@LOGGER.debug{"\t\tFound '#{dependency}' in common resources directory"}
            elsif (File.exists? in_home)
                @filename_map[dependency] = in_home
                @@LOGGER.debug{"\t\tFound '#{dependency}' in the project home directory"}
            elsif (File.exists? in_test_home)
                @filename_map[dependency] = in_test_home
                @@LOGGER.debug{"\t\tFound '#{dependency}' in the testing home directory (for submit server use)"}
            else
                puts "Couldn't find dependency '#{dependency}'"
                exit 2
            end
        }

        @@LOGGER.debug{"All files validated, ready to test!"}
    end

    def integrity_exit(message)
        @@LOGGER.fatal{"TEST FILE INCONSISTENCY: #{message}"}
        exit 2
    end

    # Runs all modules for this test
    def run
        @to_cleanup = []

        at_exit {
            @to_cleanup.each{|e|
                @@LOGGER.debug{"Cleaning local dependency '#{e}'"}
                FileUtils.rm e
            }
        }

        fail_flag = false

        # Copy in all local dependencies
        # Local dependencies should be used for things like dependencies to an OCaml file
        @local_resources.each{|resource|
            @@LOGGER.debug{"\t\tPulling local resource '#{resource}'"}

            # Pull the local dependency into the test directory
            FileUtils.cp(@filename_map[resource], get_test_path)
            # Since it's local and relatively pathed, it no longer needs to be remapped
            @filename_map.delete(resource)
            # We also need to clean up this file in the end
            @to_cleanup.push File.join(get_test_path, resource)
        }

        # Actually run the tests
        @test_config.modules.each{|mod, command|
            @@LOGGER.info{"Running #{mod}"}

            @@LOGGER.debug{"\tApplying resource mapping"}

            ################################
            # Actually run the test module #
            ################################

            # Store the paths for the three possible files
            student_output = get_student_output_filename(mod)
            student_error = get_student_error_filename(mod)
            expected_output = get_expected_output_filename(mod)

            # Run the test command and capture student outputs
            @@LOGGER.debug{"\tRunning test command: '#{command}'"}
            stdout, stderr, status = nil, nil, nil
            Dir.chdir(get_test_path) do
              stdout, stderr, status = Open3.capture3(command)
            end

            # Output student stdout
            @@LOGGER.debug{"\tOutputting student stdout to #{student_output}"}
            f = File.open(student_output, "w")
            f << stdout
            f.close

            # Delete old student error if it exists
            @@LOGGER.debug{"\tOutputting student stderr to #{student_error}"}
            if File.exists?(student_error)
                File.delete(student_error)
            end

            # If we're not in notrace and there's something in stderr, write it
            if !@leavenotrace && stderr != nil && stderr != ""
                e = File.open(student_error, "w")
                e << stderr
                e.close
            end

            if status == 0
                @@LOGGER.debug{"\tComparing '#{student_output}' with '#{expected_output}'"}
                error_message = do_comparison(expected_output, student_output)
            else
                #@@LOGGER.debug{"\tThe test command exited with a non-zero status"}
                #@@LOGGER.debug{"STDOUT:"}
                #@@LOGGER.debug{`cat #{student_output}`}
                #@@LOGGER.debug{"STDERR:"}
                #@@LOGGER.debug{`cat #{student_error}`}
                error_message = "A non-zero exit status was received"
            end

            unless error_message == nil
                @@LOGGER.info{"\tModule #{mod} has failed!"}
                @@LOGGER.info{"\tError message: '#{error_message}'"}
                fail_flag = true
            else
                @@LOGGER.info{"\t#{mod} has passed!"}
            end

            #If in notrace mode, delete output files
            if @leavenotrace
                @@LOGGER.debug{"\tPerforming output cleanup (in notrace mode)"}

                [student_output, student_error].each{|f|
                    @@LOGGER.debug{"\t\tRemoving #{f}"}

                    if (File.exists?(f))
                        File.delete(f)
                    end
                }
            end
        }

        unless fail_flag
            @@LOGGER.info{"Test passed!"}
            exit 0
        else
            @@LOGGER.info{"Test failed!"}
            exit 1
        end
    end

    ######################################################################
    # Getters for filenames in case this kind of structure changes later #
    # ####################################################################

    @@TEST_CONFIG_FILENAME = "test.yml"
    @@EXPECTED_OUTPUT_FILENAME = "%s.expected"
    @@STUDENT_OUTPUT_FILENAME = "%s.output"
    @@STUDENT_ERROR_FILENAME = "%s.error"

    def get_test_path
        return @test_dir
    end

    def get_test_config_filename
        return File.join(get_test_path, @@TEST_CONFIG_FILENAME)
    end

    def get_expected_output_filename(module_name)
        return File.join(get_test_path, @@EXPECTED_OUTPUT_FILENAME % module_name)
    end

    def get_student_output_filename(module_name)
        return File.join(get_test_path, @@STUDENT_OUTPUT_FILENAME % module_name)
    end

    def get_student_error_filename(module_name)
        return File.join(get_test_path, @@STUDENT_ERROR_FILENAME % module_name)
    end
end
