#!/usr/bin/env ruby

require_relative "../../framework/test.rb"

TestCase.new("simple", "public", File.dirname(__FILE__), 1).run
