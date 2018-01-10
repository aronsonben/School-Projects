require_relative "test_utils.rb"
require "yaml"
require "fileutils"
require "logger"
require "open3"

class TestCase
    @@MODULE_DIR_PREFIX = "module"
    @@MODULE_CONFIG_FILE = "module.yml"
    @@MODULE_EXPECTED_OUTPUT_FILE = "expected_output"
    @@MODULE_STUDENT_OUTPUT_FILE = "student_output"
    @@MODULE_STUDENT_ERROR_FILE = "student_error"

    # Create a logger that really functions more like a discretionary print system
    @@LOGGER = Logger.new(STDOUT)
    @@LOGGER.level = Logger::INFO
    @@LOGGER.formatter = proc { |severity, datetime, progname, msg|
        "#{msg}\n"
    }

    # All we need to know to run a test case is:
    #   - What it's called
    #   - Where it is
    #   - How many modules it has
    def initialize(test_name, test_type, test_dir, num_modules)

        @leavenotrace = false

        # Parse flags like a caveman
        ARGV.each{|flag|
            # -d switches into debug mode
            if (flag == "-v")
                @@LOGGER.level = Logger::DEBUG
                @@LOGGER.info{"Starting in verbose mode"}
            elsif (flag == "-nt")
                @@LOGGER.info{"Starting in notrace mode (auto-deletes output files)"}
                @leavenotrace = true
            end
        }

        @test_name = test_name
        @test_type = test_type

        @@LOGGER.info{"================================================="}
        @@LOGGER.info{"Running test: #{test_type} #{test_name}"}
        @@LOGGER.debug{"Initializing testing setup..."}

        # Ensure that the test directory exists
        unless File.directory?(test_dir)
            @@LOGGER.fatal{"Test directory (#{test_dir}) was not found"}
            exit 2
        else
            @test_dir = test_dir
            if test_dir =~ /^\.\//
                @test_dir = test_dir[2..-1]
            elsif test_dir =~/^\.\\\\/
                @test_dir = test_dir[3..-1]
            end

            @@LOGGER.debug{"\tTest directory: #{@test_dir}"}
        end

        @num_modules = num_modules
        @module_configs = Array.new(@num_modules)
        @@LOGGER.debug{"\tNumber of test modules: #{@num_modules}"}

        ##############################################
        # Validate that all necessary files are here #
        ##############################################

        @@LOGGER.debug{"Validating module structure..."}


        # Ensure that every module has the necessary components
        @num_modules.times{|i|
            @@LOGGER.debug{"\tValidating files for module #{i}... "}
            unless File.directory?(get_module_dir i)
                integrity_exit "Missing module directory for module #{i}" 
            end
            unless File.exists?(get_module_config_file i)
                integrity_exit "Missing module config file for module #{i}"
            end
            unless File.exists?(get_module_output_file i)
                integrity_exit "Missing module output file for module #{i}" 
            end

            # Load the config file
            @module_configs[i] = YAML::load_file(get_module_config_file i)

            # This might not be how this works
            if @module_configs[i] == nil
                integrity_exit "Couldn't parse config file for module #{i}"
            end

            @@LOGGER.debug{"\t\tModule #{i} is valid!"}
        }

        @@LOGGER.debug{"All files validated, ready to test!"}
    end

    def integrity_exit(message)
        @@LOGGER.fatal{"TEST FILE INCONSISTENTY: #{message}"}
        exit 2
    end

    # Runs all modules for this test
    def run
        fail_flag = false

        @num_modules.times{|module_num|
            @@LOGGER.info{"Running module #{module_num}"}

            ###################################
            # Poor man's dependency injection #
            ###################################

            # Map to absolute paths
            filename_map = {}
            module_dir = get_module_dir(module_num)


            @to_cleanup = []
            at_exit{
                @to_cleanup.each{|e|
                    @@LOGGER.debug{"Cleaning local dependency '#{e}'"}
                    FileUtils.rm e
                }
            }

            # First, find dependencies and remap
            standard_dependencies = get_config_attribute(module_num, "dependencies")
            local_dependencies = get_config_attribute(module_num, "local_dependencies")

            standard_dependencies = [] if standard_dependencies == nil
            local_dependencies = [] if local_dependencies == nil

            (standard_dependencies + local_dependencies).each{|dependency|
                @@LOGGER.debug{"\tSearching for dependency '#{dependency}'"}

                filename_map[dependency] = File.join(module_dir, dependency)

                in_local = filename_map[dependency]
                in_typed = File.join(module_dir, "..", "..", "..", "common", @test_type, dependency)
                in_all = File.join(module_dir, "..", "..", "..", "common", "all", dependency)
                in_home = File.join(module_dir, "..", "..", "..", "..", dependency)
                in_test_home = File.join(module_dir, "..", "..", "..", dependency)

                if (File.exists? in_local)
                    @@LOGGER.debug{"\t\tFound '#{dependency}' in module directory"}
                elsif (File.exists? in_typed)
                    filename_map[dependency] = in_typed
                    @@LOGGER.debug{"\t\tFound '#{dependency}' in #{@test_type} resources directory"}
                elsif (File.exists? in_all)
                    filename_map[dependency] = in_all
                    @@LOGGER.debug{"\t\tFound '#{dependency}' in common resources directory"}
                elsif (File.exists? in_home)
                    filename_map[dependency] = in_home
                    @@LOGGER.debug{"\t\tFound '#{dependency}' in the project home directory"}
                elsif (File.exists? in_test_home)
                    filename_map[dependency] = in_test_home
                    @@LOGGER.debug{"\t\tFound '#{dependency}' in the testing home directory (for submit server use)"}
                else
                    puts "Couldn't find dependency '#{dependency}'"
                    exit 2
                end

                # If it's a local dependency, copy it in and mark it for cleanup
                # Local dependencies should be used for things like dependencies to an OCaml file
                if local_dependencies.include?(dependency)
                    @@LOGGER.debug{"\t\tPulling local dependency '#{dependency}'"}
                    #stdout, stderr, status = Open3.capture3("cp #{filename_map[dependency]} #{in_local}")
                    FileUtils.cp(filename_map[dependency], in_local)
                    filename_map.delete dependency
                    @to_cleanup.push in_local
                end
            }

            @@LOGGER.debug{"\tApplying resource mapping"}

            # Substitute absolute paths in for file names
            command = get_config_attribute(module_num, "command")
            filename_map.each_pair{|k, v| command.sub!(k, v)}

            ################################
            # Actually run the test module #
            ################################

            student_output = get_student_output_file(module_num)
            student_error = get_student_error_file(module_num)
            expected_output = get_module_output_file(module_num)

            # Run the test command and capture student outputs
            @@LOGGER.debug{"\tRunning test command: '#{command}'"}
            stdout, stderr, status = Open3.capture3(command)

            # Output student stdout
            @@LOGGER.debug{"\tOutputting student stdout to #{student_output}"}
            f = File.open(student_output, "w")
            f << stdout
            f.close

            # Output student stderr
            @@LOGGER.debug{"\tOutputting student stderr to #{student_error}"}
            if File.exists?(student_error)
                File.delete(student_error)
            end

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
                @@LOGGER.info{"\tModule #{module_num} has failed!"}
                @@LOGGER.info{"\tError message: '#{error_message}'"}
                fail_flag = true
            else
                @@LOGGER.info{"\tModule #{module_num} has passed!"}
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

    def get_config_attribute(modulenum, attribute)
        @module_configs[modulenum][attribute]
    end

    def get_module_dir(modulenum)
        return File.join(@test_dir, "#{@@MODULE_DIR_PREFIX}#{modulenum}")
    end

    def get_module_config_file(modulenum)
        return File.join(get_module_dir(modulenum), @@MODULE_CONFIG_FILE)
    end

    def get_module_output_file(modulenum)
        return File.join(get_module_dir(modulenum), @@MODULE_EXPECTED_OUTPUT_FILE)
    end

    def get_student_output_file(modulenum)
        return File.join(get_module_dir(modulenum), @@MODULE_STUDENT_OUTPUT_FILE)
    end

    def get_student_error_file(modulenum)
        return File.join(get_module_dir(modulenum), @@MODULE_STUDENT_ERROR_FILE)
    end
end
