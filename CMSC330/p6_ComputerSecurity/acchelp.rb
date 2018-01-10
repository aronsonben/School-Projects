# Account help

@accounts2=[]

class Account
  attr_accessor :user
  attr_accessor :directory
  attr_accessor :password
  attr_accessor :socket
  attr_accessor :public_files
  def initialize(user,password,directory)
    @user = user
    @password = password
    @directory = directory
  end
end

a=Account.new("josh","password","cd/josh")
@accounts2.push(a)

puts @accounts2[0].password