require "yaml"

class TestConfig
    @@NAME = "name"
    @@TYPE = "type"
    @@RESOURCES = "resources"
    @@LOCAL_RESOURCES = "local_resources"
    @@MODULES = "modules"

    def initialize(filename)
        @config = YAML::load_file(filename)
    end

    def name
        @config[@@NAME]
    end

    def type
        @config[@@TYPE]
    end

    def modules
        @config[@@MODULES]
    end

    def resources
        @config[@@RESOURCES]
    end

    def local_resources
        @config[@@LOCAL_RESOURCES]
    end
end
