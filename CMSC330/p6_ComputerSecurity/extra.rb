# extra

# path to accounts - "make sure you reject the illegal paths"

#----------------------------------------#
# Small things

# print @s2a info (user and socket)
if @s2a[socket]
str1 = [@s2a[socket].user, @s2a[socket].socket].join(' ')
my_send(socket, str1)
end


# ls

flst = files.split(" ")
flst.each { |f| 
	my_send(socket, f.to_s)
}

# my_send(socket, "Index: \n#{ `ls "#{path}/"` }")
# my_send(socket, "Current Dir: \n#{ Dir.getwd } ")--`ls #{path}`

# get

# # ^public\/$|public\/([a-zA-z0-9\_\-]+\.*[a-zA-z0-9\_\.\-]+)$
# if path =~ /^public\/$|public\/(\.{0,2}[\w\-\_]+[\w\-\_\.]*$|\.{3,}[\w\-\_\.]*)$/ then
# full_path = File.join(Dir.pwd,"users",path)
# else # /^\.{0,2}[\w\-\_]+[\w\-\_\.]*$|^\.{3,}[\w\-\_\.]*$/
# full_path = File.join(@s2a[socket].directory,path)
# end

#----------------------------------------#


# ls more
buffer = Buffer.new()

len = fstr2.length
while len > 0
	if len > 31 then
		str = fstr2.slice!(0..31)
		buffer.write(str)
		len = len - 32
		# my_send(socket,str)
	else 
		str = fstr2.slice!(0..len)
		buffer.write(str)
		len = 0
		# my_send(socket,buffer.getBuf.to_s)	
	end
end
# my_send(socket, buffer.getBuf.to_s)



# files = Dir["*"]
# files.sort!
# farr = []
# files.each { |f|
	# fs = "#{f}\n"
	# farr << fs
# }
# fstr = farr.join
# fstr.chomp!
# fstr2 = "Index: \n#{fstr}"
# my_send(socket, fstr2)
# return

# if fstr.length < 25 then # didn't work
	# my_send(socket, "Index:\n#{fstr}")
# else
	# my_send(socket, "#{fstr.length} length")
# end

# my_send(socket, "Index:\n#{fstr}\n")
# fstr = "Index: \n"
# files.each { |f|
	# f << "\n"
	# fstr << f
# }
# my_send(socket, "Index: \n#{files.join("\n")}")

# fstr = ""
# files.each { |f|
	# fs = "#{f}\n"
	# fstr = fstr + fs
# }
# fstr2 = "Index: \n#{fstr}"
# my_send(socket, fstr2)



# ls extra - passing all public - failing buffer overflow
def ls(arg,socket)
  unless logged_in socket
    my_send(socket, "You're not logged in yet")
    return
  end
  if arg =~ /^public$/ then
    path = File.join(Dir.pwd,"users",arg)
	Dir.chdir(path) do
		files = Dir["*"]
		files.sort!
		fstr = "Index: \n"
		files.each { |f|
			f << "\n"
			fstr << f
		}
		my_send(socket, fstr)
	end
  elsif arg == "" then
    path = @s2a[socket].directory
	Dir.chdir(path) do
		files = Dir["*"]
		files.sort!
		fstr = "Index: \n"
		files.each { |f|
			f << "\n"
			fstr << f
		}
		my_send(socket, fstr)
	end
	
	# my_send(socket, "Index: \n#{`ls "#{path}"`}")
  else
    my_send(socket, "Listed directory can be only none or public")
  end
end













# 'Get' extra
# pwd = Dir.getwd
# Dir.chdir(@s2a[socket].directory) do
	# my_send(socket,"Got file #{path}")
	# my_send(socket,IO.read(full_path))
# end
# my_send(socket,"Got file #{path}")
# data = nil
# File.open(full_path, "rb") { |fp| 
	# pwd = @s2a[socket].directory
	# Dir.chdir(pwd) do
		# if !Dir.entries(pwd).include? name then
			# data = fp.read
			# my_send(socket, data)
		# else
			# my_send(socket, "Overwriting file")
		# end
	# end
# }
# if data then my_send(socket, data)
# else my_send(socket, "Invalid data") end




#Original Put (part of)
data = my_recv socket
if data != "IGNORE"
if path =~ /^public/ then
  full_path = File.join(Dir.pwd,"users",path)
  if File.exists?(full_path)
	my_send(socket, "You do not have permissions to put that file there")
	return
  end
else
  full_path = File.join(@s2a[socket].directory,path)
end
File.open(full_path, "wb") do |file|
  File.write(file,data)
end
my_send(socket, "Put file #{path}, size #{File.size full_path} bytes")
end

#Original User
def user(arg,socket)
  arg.downcase!
  found=false
  if !arg || arg==""
    my_send(socket, "No username supplied")
    return
  end
  if @s2a[socket] && @logged_in && @s2a[socket].user == arg	  # Error here - never outputs this
	my_send(socket, "User already logged in")
    return
  end
  @accounts.each{|a|
    if(a.user==arg)
      @s2a[socket]=a
      found=true
      my_send(socket, "Welcome #{arg}")
    end
  }
  if !found
    my_send(socket, "Username #{arg} not found")
  end
end

#Idea 1
def user(arg,socket)
  arg.downcase!
  found=false
  if !arg || arg==""
    my_send(socket, "No username supplied")
    return
  end
  if logged_in socket
	# Could be an error if AFTER a user is signed in, call "user <unknown username>" - should it go here or to "user not found"?
	my_send(socket, "User already logged in")
    return
  end
  @accounts.each{|a|
    if(a.user==arg)
      @s2a[socket]=a
      found=true
	  my_send(socket, "Welcome #{arg}")
    end
  }
  if !found
    my_send(socket, "Username #{arg} not found")
  end
end

