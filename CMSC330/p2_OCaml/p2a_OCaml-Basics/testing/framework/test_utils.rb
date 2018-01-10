# Helper for comparing two files
def compare_files(canonical_file, student_file)
    while true
        canonical_line = canonical_file.gets
        while canonical_line && canonical_line !~ /\S/	# no non-whitespace char
            canonical_line = canonical_file.gets
        end

        student_line = student_file.gets
        while student_line && student_line !~ /\S/
            student_line = student_file.gets
        end

        return nil if (canonical_line == nil) && (student_line == nil)

        # If the canonical file and student file end at different times, fail
        if (canonical_line != nil) && (student_line == nil)
            return "expecting more output!"
        end

        if (canonical_line == nil) && (student_line != nil)
            return "too much output!"
        end

        canonical_line.strip! 	# remove leading/training whitespace
        student_line.strip! 	# remove leading/training whitespace

        cStr = canonical_line.split(" ").join(" ")  # normalize internal whitespace
        sStr = student_line.split(" ").join(" ")	# normalize internal whitespace

        if cStr != sStr
            return "found non-matching output lines"
        end
    end
end

def do_comparison(canonical_filename, student_filename)
    File.open(student_filename, "r") do |student_file|
        File.open(canonical_filename, "r") do |canonical_file|
            return compare_files(canonical_file, student_file)
        end
    end

    return "couldn't open output files!"
end
