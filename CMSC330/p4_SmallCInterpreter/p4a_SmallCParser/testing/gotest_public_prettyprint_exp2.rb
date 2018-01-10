#!/usr/bin/env ruby
require_relative "framework/TestCase.rb"
TestCase.new(File.join(File.dirname(__FILE__), "public_prettyprint_exp2")).run
