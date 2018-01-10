# Wordnet2 #

class WordNet
    #TODO
	attr_accessor :vertices, :id_arr
	
	def initialize(synsetsfile, hypernymsfile)
		@synsetsfile = synsetsfile
		@hypernymsfile = hypernymsfile
		@vertices = {}
		
		@id_arr = []
		@nounlist = [] 
		
		
		
		# Synset check # 
		syn_file = open(@synsetsfile)

		invalid_syn = false
		invalid_syns = []

		idcount = 0
		il_index = 0
		File.foreach(syn_file) do |line|
			
			linechomp = line.chomp
			if line =~ /^id:\s(\d+)\ssynset:\s((\d+|\w+|[\'\"\-\/\?\_]+)+(,(\d+|\w+|[\'\"\-\/\?\_\.]+)*)*)/
				# print $1, ": ", $2, "\n"
				
				# if $1.to_i != idcount # make sure ids are in order 1->n
					# invalid_syn = true
					# invalid_syns[il_index] = linechomp
					# il_index += 1
				# end
				@id_arr[idcount] = $1.to_i
				@nounlist[idcount] = $2
				
			else		# invalid line
				invalid_syn = true
				invalid_syns[il_index] = line
				il_index += 1
				
			end
			
			idcount += 1
		end

		# this indicates there is at least one invalid line in the synset file, so exit 
		if invalid_syn == true
			puts "invalid synsets"
			
			invalid_syns.each do |badline|
				puts badline
			end
			
			# invalidsynsets = []
			# index = 0 
			# invalid_syns[index] = "invalid synsets\n"
			# index += 1
			# invalidsynsets.each do |badline|
				# bl2 = "#{badline}"
				# invalidsynsets[index] = bl2
				# index += 1
			# end
			
			# return invalidsynsets
			
			exit(0)
		end
		
		syn_file.close

		# puts "{PASSED} Synsets"

		
		@fromto = []
		@fromlist = []
		@tolist = []
		
		
		hyp_file = open(@hypernymsfile)
		invalid_hyp = false
		invalid_hyps = []
		
		ih_index = 0
		listindex = 0
		File.foreach(hyp_file) do |line|
		
			linechomp = line.chomp
			if linechomp =~ /^from:\s(\d+)\sto:\s(\d+(,\d+)*)/
				# print $1, "->", $2, "\n"
				spl = []
				invalidtos = false
				
				# check for invalid to ids -- DON"T NEED THIS
				# if $2.include? "," 
					# spl = $2.split(',') 
					# spl.each { |s|
						# if !(@id_arr.include? s.to_i)
							# invalidtos = true
						# end
					# }
				# else
					# if !(@id_arr.include? $2.to_i)
						# invalidtos = true
					# end
				# end
				
				# elsif invalidtos == true
					# invalid_hyps[ih_index] = line
					# invalid_hyp = true
					# ih_index += 1
				
				
				# checking for valid from and to ids together
				# if !(@id_arr.include? $1.to_i)		# check from id is valid - don't need this either?
					# invalid_hyps[ih_index] = line
					# invalid_hyp = true
					# ih_index += 1
				# else
					# valid, so do all the info stuff
					
				@fromto[listindex] = [$1.to_i, $2]
				@fromlist[listindex] = $1.to_i
				@tolist[listindex] = $2
				listindex += 1
					
				# end
				
			else		# invalid formatting
				invalid_hyps[ih_index] = linechomp
				invalid_hyp = true
				ih_index += 1
				
				# puts invalid_hyps
			end
		end

		# this indicates there is at least one invalid line in the synset file, so exit 
		if invalid_hyp == true
			puts "invalid hypernyms"
			
			invalid_hyps.each do |badline|
				puts badline
			end
			
			exit(0)
		end

		hyp_file.close
		
		# puts "{PASSED} Hypernyms"

		# puts @fromlist.to_s, @tolist.to_s
		
		@node_arr = []

		i = 0
		(0..id_arr.length-1).each { |i|
			comma = []
			node = {}
			
			node[:id] = @id_arr[i]
			
			comma = comma_check(@nounlist[i])
			node[:nouns] = comma
			
			
			node[:fromlist] = @fromlist[i]
			node[:tolist] = []
			
			# puts @fromto.to_s
			
			node[:tolist] = []
			
			# fromto = [fromid, "toids"]
			@fromto.each { |ft|
			
				if ft[0] == @id_arr[i]
					node[:fromlist] = ft[0]
					
					comma = comma_check(ft[1])
						
					if comma.is_a? Array 
						comma.each { |i|
							node[:tolist] << i
						}
					else
						node[:tolist] << comma
					end
				end
			
			}
			@node_arr[i] = node
		}

		@node_arr.each { |node| 
			add_vertex(node)
		}
		
		# Creating edges for every vertex that needs it 
		@edgecount = 0 		# keeps track of # of edges
		
		ii = 0
		to_varr = []
		
		@vertices.each { |vptf|
			#initialize the point_to and point_from for the "from_vertex" here
			vptf[1][:point_to] = []
			vptf[1][:point_from] = []
		}
		
		# go through each vertex and create the necessary edges
		@vertices.each { |vertex|
			to_varr[ii] = []
			
			# print vertex[0], " ", vertex[1][:to_ids], "\n"
			
			if vertex[1][:tolist].length != 0 
			
				
				to_vtx_ids = vertex[1][:tolist]					
				
				# find to_vertex
				to_vtx_ids.each { |v|
				
					@vertices.each { |dagv| 
						
						
						if dagv[0] == v.to_i
							
							# found the to_vertex
							to_v = dagv
							
							# puts to_v.to_s
							# print vertex[0]," ", to_v[0], "\n"
							
							# to_v[1][:point_to] = []
							# to_v[1][:point_from] = []
							
							create_edge(vertex, to_v)
							@edgecount += 1
							
							# puts "...out of the method..."
							# puts "fv->#{vertex[1][:point_to]}"		# updated
							# puts "fv<-#{vertex[1][:point_from]}"	# NOT updated
							# puts "tv->#{to_v[1][:point_to]}"		# NOT updated
							# puts "tv<-#{to_v[1][:point_from]}"		# updated				
							
							# print vertex[1][:point_to]," ", to_v[1][:point_from].to_s, "\n"
							to_varr[ii] << to_v[1][:point_from]
							
						end
					}
				}
			else
				# puts "Vertex #{vertex[0]} has nothing to point to!"
			end
			ii += 1
		}
		
		
		# add the "point_from" parameter
		i = 0
		@vertices.each { |x|
			t = x[1][:point_to]
			
			# print x[0], " - points to: ", t, "\n"
			t.each { |tt|
				# print "Going through #{tt.to_s} out of #{t.to_s}\n"
				
				@vertices.each { |x2|
					if tt == x2[0]
						tt = x2
					end
				}
				
				if !(tt[1][:point_from].include? x[0])
					tt[1][:point_from] << x[0]
				end
			}
		}
		
		# end of make_edges

		# display
		# puts "-~" * 10, "-~" * 10, "-~-~-~      -~-~-~-~", "-~" * 10, "-~" * 10
		
		
		
		
		
		
		
		
		
		
				
	end # end of intialize
	
	# method to help check if the "from: " or "to: " id exists in the synset file
	def id_check(ele, id_arr)
		valid = false
		if !(ele.nil?)
			
			# check for ele being an Integer, not a String (or something else)
			result = Integer(ele) rescue false
			
			if result != false
				if id_arr.include? result.to_i
					valid = true
					return valid
				end
			end
		end
	end

	# check if info needs to be split at a comma (nouns, to_ids)
	def comma_check(ele)
		comma = []
		
		if ele.include?(',')
			# contains comma. split it and return the split array 
			comma = ele.split(',')
			return comma
		else
			# no comma. don't split, but I would like to put it in an array
			# comma[0] = ele  --- if i want everything in array format uncomment this and return comma
			return ele 
		end
	end

	
	######## calling methods ########
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	####### GRAPH CREATION FUNCTIONS #######
	# add a new vertex to the WordNet
	def add_vertex(vertex)
		# (vertex is a node) store by ID
		@vertices[vertex[:id]] = vertex
	end
	
	# create an edge between vertices in WordNet
	def create_edge(fv, tv)
		# creating an "edge" from one vertex to another to show they are 'directed' (hence DAG)
		# Example: c -> a -> d  --- a[:point_to] = d || a[:point_from] = c
		
		fv[1][:point_to] << tv[0]
		tv[1][:point_from] << fv[0]
	end
	
	# display the current WordNet graph
	def display
		@vertices.each { |v|
			# print "Index: #{v[0]} || Vertex Info = [ id: #{v[1][:id]}  nouns: #{v[1][:nouns]}\t\tfrom_id: #{v[1][:from_id]}\tto_ids: #{v[1][:to_ids]}\t\tpoint_to: #{v[1][:point_to]}\t\tpoint_from: #{v[1][:point_from]} ]\n"
			print v, "\n"
		}
	end
	##########################################
	
	
	####   ####   ####   ####   ####   ####   ####   ####
	###### COMMAND MODE FUNCTIONS #######
	
	# method to check that all nouns in input file exist in total list of nouns
	def isnoun(nouns)
		# puts nouns.to_s
		puts @nounlist.to_s
		rawnouns = []
		@nounlist.each { |nou|
			comma = []
			comma = comma_check(nou)
			
			if comma.is_a? Array
				comma.each { |nx| rawnouns << nx }
			else
				rawnouns << nou
			end
		}
		# puts rawnouns.to_s
		
		count = 0
		foundallnouns = false
		nouns.each { |n|
			if rawnouns.include? n
				count += 1
			end
			if count == nouns.length 
				foundallnouns = true
			end
		}
		return foundallnouns
	end
	
	# method to return number of nouns in the synsets
	def nouns
		count = 0
		
		@nounlist.each { |noun|
			comma = []
			comma = comma_check(noun)
			# puts comma.to_s
			
			if comma.is_a? Array 
				comma.each { |n|
					count += 1
				}
			else 
				count += 1
			end
		}
		# not sure if this does return or puts 
		return count 
		#print count
	end
	
	# method to return the number of edges in the graph 
	def edges
		return @edgecount
	end
	####   ####   ####   ####   ####   ####   ####   ####
	
	
	
	############ PART 3 ###################
	
	### Helpers ### 
	
	# help for displaying the path arrays neater
	def dispath(path)
		print "[ "
		path.each { |z|
			print z[0], " "
		}
		print "]\n"
	end
	
	# quick helper to assign the id to the correct vertex 
	def assign(v_id)
		@vertices.each { |ve|
			if ve[0] == v_id
				v_id = ve
			end
		}
		return v_id
	end

	
	#### Required methods #####
	
	# length method #
	def length(v, w)
		
		# checking for return -1 cases
		if !(v.is_a? Array)
			v_ = [v]
			v = v_
		end
		if !(w.is_a? Array)
			w_ = [w]
			w = w_
		end
		if v.length == 0 or w.length == 0 
			return -1 
		end 
		v.each { |vx| if !(id_arr.include? vx) then return -1 end }
		w.each { |wx| if !(id_arr.include? wx) then return -1 end }
		
		# # # BFS # # #		# # # BFS # # #		# # # BFS # # #
		@alllengths = []
		
		@vlevs = {}
		@wlevs = {}
		i = 0
		
		# stores SAPs
		@saps = {}
		@sapsdex = 0
		
		# reminder: will need to do .each (if needed) for v, w
		v.each { |ve|
			vv = assign(ve)
			# puts "-------------", vv.to_s, "---------------"
			
			w.each { |we|
				ww = assign(we)
				i = 0
				# puts ww.to_s, "current i is: #{i}"
				
				
				@vlevs[i] = [vv]
				@wlevs[i] = [ww]
				# puts @vlevs, @wlevs
				
				# reminder: will need to do .each (if needed) for vv.point_to, ww.point_to
				vv_to = []
				ww_to = []
				
				j = 0
				vv[1][:point_to].each { |vvpt|
					a = assign(vvpt)
					vv_to[j] = a
					j += 1
				}
				j = 0
				ww[1][:point_to].each { |wwpt|
					a = assign(wwpt)
					ww_to[j] = a
					j += 1
				}
				
				vnextlev = []
				wnextlev = []
				vv_to.each { |vvt|
					vnextlev << vvt
				}
				ww_to.each { |wwt|
					wnextlev << wwt
				}
				
				# Variation of breadth first search starting at vertex v
				while vnextlev.length != 0
					i += 1
					@vlevs[i] = []
					
					# puts "Next level array (#{i}):"
					# dispath(vnextlev)
					vnextlev.each { |n| @vlevs[i] << n }
					# puts "V Level array at v[#{i}]:"
					# dispath(@vlevs[i])
					
					@vlevs[i].each { |x|
						# puts "Entered into: #{x.to_s}"
						
						if x[1][:point_to].length != 0
							# only enter if it has a point_to 
							
							vnextlev.shift
							# puts "Pop!"
							# dispath(vnextlev)
							
							x[1][:point_to].each { |xpt| 
								xx_to = assign(xpt)		# note will need to change the [0] if more than one point_to					
								vnextlev << xx_to
								# puts "Updated V-Next level array at i=#{i}:"
								# dispath(vnextlev)
							}
							
						else 
							# puts "#{x[0]} points to nothing"
							vnextlev.shift
							# puts "Pop!"
							# dispath(vnextlev)
						end
					}
					# puts "--"
				end
				
				i = 0
				# Variation of breadth first search starting at vertex w
				while wnextlev.length != 0
					i += 1
					@wlevs[i] = []
					
					# puts "Next level array (#{i}):"
					# dispath(wnextlev)			
					wnextlev.each { |n| @wlevs[i] << n }			
					# puts "W Level array at w[#{i}]:"
					# dispath(@wlevs[i])
					
					@wlevs[i].each { |x|
						# puts "Entered into: #{x.to_s}"
						
						if x[1][:point_to].length != 0
							# only enter if it has a point_to 
							
							wnextlev.shift
							# puts "Shift!"
							# dispath(wnextlev)
							
							x[1][:point_to].each { |xpt| 
								xx_to = assign(xpt)		# note will need to change the [0] if more than one point_to					
								
								if !(wnextlev.include? xx_to) then wnextlev << xx_to end 
								# wnextlev << xx_to
								
								# puts "Updated W-Next level array at i=#{i}:"
								# dispath(wnextlev)
							}
							
						else 
							# puts "#{x[0]} points to nothing"
							wnextlev.shift
							# puts "Shift!"
							# dispath(wnextlev)
						end
					}
					# puts "--"
				end
				
				pathlengths = {}
				lendex = 0
				
				# Now search through each and find elements contained in both
				@vlevs.each { |v1|
					
					# puts v1.to_s					# entire entry 
					# puts v1[1][0][0] 				# index
					# puts v1[1][0][1][:nouns]		# get hash entry
					
					# print "Elements at #{v1[0]} are #{v1[1][0]}\n"

					abc = 0
					v1[1].each { |v2|
						
						# puts "At level #{v1[0]}, using #{v2.to_s}"
						
						@wlevs.each { |w1|
							# puts w1.to_s
							
							# puts "At W level #{w1[0]}, contains:"
							# dispath(w1[1])
							w1[1].each { |w2|		# go through each element at each level
								
								if v2[0] == w2[0]
								
									# puts "entered, #{v2[0]}"
									
									# puts "hey, #{v1[1][0][0]} equals #{w1[1][0][0]}"
									# puts "The length from #{v[0]} to #{v1[1][0][0]} is: #{v1[0]}"
									# puts "The length from #{w[0]} to #{w1[1][0][0]} is: #{w1[0]}"
									
									combined = v1[0] + w1[0]
									# puts "SAP for #{w1[1][0][0]} is #{combined}"
									
									# puts combined
									pathlengths[lendex] = []
									
									
									# print "Get path for ", v2[0], ", ", w2[0], "\n"
									# path = getpath(v2[0], w2[0])
									
									# looking for v2[0] and w2[0] 
									vpath = []
									wpath = []
									
									basev = @vlevs[0][0]
									basew = @wlevs[0][0]
									
									@vlevs.each { |lvl| 
									
										lvl[1].each { |l2|		# puts lvl.to_s
											
											# puts l2.to_s
											basev = l2
											
											if basev[0] == v2[0]
												if !(vpath.include? l2) then vpath << l2 end
												break
											end
										}
										
										if basev[0] == v2[0]
											# puts "Found: ", basev[0]
											break
										end	
										
										if !(vpath.include? basev) then vpath << basev end
									}
									# puts vpath.to_s
									# dispath(vpath)
									
									# puts "--"
									@wlevs.each { |lvl| 
									
										lvl[1].each { |l2|		# puts lvl.to_s
											
											# puts l2.to_s
											basew = l2
											
											if basew[0] == w2[0]
												
												if !(wpath.include? l2) then wpath << l2 end
												
												break
											end
										}
										
										if basew[0] == w2[0]
											# puts "Found: ", basew[0]
											break
										end	
										
										if !(wpath.include? basew) then wpath << basew end
									}
									# puts wpath.to_s
									# dispath(wpath)
									
									fullpath = []
									fullpath << vpath
									fullpath << "x"
									fullpath << wpath
									path = fullpath
									
									# concatenate the path because it is split at the moment
									pathconcat = []
									path.each { |arr|
										if arr.is_a? String 
											arr2 = arr.each_char.to_a 
											pathconcat.concat(arr2)	
										else
											pathconcat.concat(arr)
										end
									}
									# dispath(pathconcat)
									
									pathlengths[lendex][0] = combined
									pathlengths[lendex][1] = pathconcat
									
									# pathlengths[lendex].each { |t| puts t.to_s }
									
									
									lendex += 1
									
									# dispath(path[0])
								end
							
							}
						}
					}
					
					# abc += 1
					# if abc == 2 then exit(0) end
				}
				
				# Find path for smallest length
				len2 = []
				path2 = []
				pathlengths.each { |p|
					# print "Index: #{p[0]}; Length: #{p[1][0]}; Path:\n"
					# dispath(p[1][1])
					
					# find path with smallest length
					len2 << p[1][0]
					path2 << p[1]
				}
				l2min = len2.min
				small = nil
				c = 0
				path2.each { |p2e| 
					if l2min == p2e[0] 		
						# find paths corresponding to minimum length for this subset
						if c != 1			
							# remove this (and change "small" to an array) if you want to accept multiple paths a shared length (i.e. multiple paths have length 3)
							small = p2e
						end
					end
				}
				
				# Now put that into a list with all of the paths of the smallest lengths for each v,w pair
				@saps[@sapsdex] = small
				@sapsdex += 1
				# dispath(small[1])
				
				len = l2min
				@alllengths << len
				
			} # end of this ww
		}
		
		
		
		
		
		
		# puts alllengths.to_s
		
		# will output minimum length
		return @alllengths.min
	end
	
	
	# ancestor method #
	def ancestor(v, w)
		
		# Paths for each SAP, keyed in a hash BY its corresponding SAP
		sappaths = {}
		lcas = []
		
		if length(v, w) != -1
			
			# puts @alllengths.to_s
			
			(0..@saps.length-1).each { |j|
				# dispath(@saps[j][1])
				
				sappaths[@alllengths[j]] = @saps[j][1]
				
				# Getting LCAs - LCA will be final element in @saps[j]. Should appear twice.
				lca = @saps[j][1][-1]
				if @saps[j][1].count(lca) == 2			# make sure it appears twice in path (coding quirk)
					if !(lcas.include? lca[0])				# it cannot already be in the lca array (no duplicates)
						# lcas << lca					# do this you want FULL vertex with hash and everything
						lcas << lca[0]
					end
				end
			}
			
			return lcas
		else 
			# Something went wrong with the lengths
			return -1
		end
		
	end
	
	
	### Root helper ### get nodes from the nouns in 'n'
	def get_nodes(n)
		gotnodes = []
		
		# extracting any arrays of nouns (for ids with multiple nouns) so we can check if a noun exists in synset file
		rawnouns = []
		@nounlist.each { |nou|
			comma = []
			comma = comma_check(nou)
			
			if comma.is_a? Array
				comma.each { |nx| rawnouns << nx }
			else
				rawnouns << nou
			end
		}
		
		if n.length > 0
		
			if n.length == 1
				
				if !(rawnouns.include? n) 	# noun 'n' is not in list of synset nouns
					return -1 
				else
					# n is valid noun, find its node
					@vertices.each { |v3|
						if v3[1][:nouns].include? n
							
							# puts "node #{v3[0]} contains #{n}"
							gotnodes << v3		# nouns can appear multiple times
						
						end
					}
					
				end
			
			else 	# n is an array, need to iterate through it 
				
				
				# might not need this??? 
				if n.is_a? String
					if !(@nounlist.include? n) 	# noun 'n' is not in list of synset nouns
						return -1 					
					else
					
						@vertices.each { |v3|
							if v3[1][:nouns].include? n
								
								# puts "node #{v3[0]} contains #{n}"
								gotnodes << v3		# nouns can appear multiple times
							
							end
						}
					end
				else
					# chars?
					n.each { |n2|
						if !(@nounlist.include? n2) 	# noun 'n' is not in list of synset nouns
							return -1 					
						else
						
							@vertices.each { |v3|
								if v3[1][:nouns].include? n
									
									# puts "node #{v3[0]} contains #{n}"
									gotnodes << v3		# nouns can appear multiple times
								
								end
							}
						end
					}
				end
				
				
			end
			
			return gotnodes
		else
			# nothing in n
			return -1
		end
		
	end
	
	# root method #
	def root(v, w)
		# print v,", ", w, "\n"
		
		finalnouns = []
		nodesv = []
		nodesw = []
		lcas = []
		
		# Check to make sure nouns are in graph will happen in "get_nodes." Get nodes for the nouns in v, w
		nodesv = get_nodes(v)
		nodesw = get_nodes(w)
		
		if nodesv != -1 and nodesw != -1
			# puts nodesv.to_s
			# puts nodesw.to_s
			
			nv = [nodesv]
			nw = [nodesw]
			
			nv.each { |nv2|			# go through each v noun
				
				nw.each { |nw2|		# go through each w noun
					
					vv = []
					ww = []
					
					nv2.each { |nv3|
						vv << nv3[1][:id]
					}
					nw2.each { |nw3|
						ww << nw3[1][:id]
					}
					
					if length(vv, ww) != -1
						
						arr = ancestor(vv, ww)
						
						arr2 = []
						arr.each { |a|
							aa = assign(a)
							lcas << aa
						}
						
					else
						return -1
					end
					
				}
			
			}
			
			# puts lcas.to_s
			lcas.each { |lca2|
				finalnouns << lca2[1][:nouns]
			}
			
			# finalnouns should hold list of nouns from all LCAs
			return finalnouns
		else
			return -1
		end 
		
	end
	
	
	
	################ PART 4 #######################
	
	# get distance helper - a and b are individual nouns (not arrays)
	def dist(a, b)
		
		dmin = length(a,b)
		# puts dmin
		if dmin == -1
			return Float::INFINITY
		else
			return dmin
		end		
	end
	
	# little helper method to help with adding the squares of distances - pass in array of distances from n1 to all n2
	def calc_outcast(dists)
		di = 0
		dists.each { |d|
			d[0].each { |d3o2| di += d3o2**2 }
		}
		return di
	end
	
	
	def outcast(nouns)
	
		fin_dist_path = []
		lengthonly = []
		
		nouns.each { |n1|
			
			fin_dists = []
			distances = []
			
			nouns.each { |n2|

				
				# print n1, " ", n2, "\n"
				
				n1nodes = []
				n2nodes = []
				
				n1nodes = get_nodes(n1)
				n2nodes = get_nodes(n2)
				
				# get ids
				n1ids = []
				n2ids = []
				
				n1nodes.each { |n1n| n1ids << n1n[1][:id] }
				n2nodes.each { |n2n| n2ids << n2n[1][:id] }
				
				# puts n1nodes.to_s
				# puts n2nodes.to_s
				
				# print n1ids, " ", n2ids, "\n"
				
				len = []
				len << dist(n1ids, n2ids)
				
				nodes_length = []
				nodes_length << len
				nodes_length << n1
				
				# puts nodes_length.to_s
				
				distances << nodes_length
				
				# print "--\n"
			
			}
			
			
			# puts "Distances: #{distances.to_s}"
			
			distn1 = calc_outcast(distances)
			# puts distn1
			
			# will be used to find max distance value
			lengthonly << distn1
			
			fin_dists << distn1
			fin_dists << distances[1][1]
			# puts fin_dists.to_s
			
			fin_dist_path << fin_dists
			
			# puts "\\/"*10
			# puts "~~~~"
		}
		
		maxes = []
		maxx = lengthonly.max
		
		# create array that has all nodes that have total distances of the max amount
		fin_dist_path.each { |fdp|
			if fdp[0] == maxx
				maxes << fdp
			end
		}
		
		# array to hold the nouns only from that ^
		maxnouns = []
		maxes.each { |mx|
			maxnouns << mx[1]
		}
		
		return maxnouns
	end
	
	
	
	
	
	
	
	
	
end




#If the result is an array, then the array's contents will be printed in a sorted and space-delimited string. 
#Otherwise, the result is printed as-is
def print_res(res)
    if (res.instance_of? Array) then 
        str = ""
        res.sort.each {|elem| str += elem.to_s + " "}
        puts str.chomp
    else 
        puts res
    end
end 

#Checks that the user has provided an appropriate amount of arguments
if (ARGV.length < 3 || ARGV.length > 5) then
  fail "usage: wordnet.rb <synsets file> <hypersets file> <command> <input file>"
end

synsets_file = ARGV[0]
hypernyms_file = ARGV[1]
command = ARGV[2]
input_file = ARGV[3]

wordnet = WordNet.new(synsets_file, hypernyms_file)

#Refers to number of lines in input file
commands_with_0_input = %w(edges nouns)
commands_with_1_input = %w(isnoun outcast)
commands_with_2_input = %w(length ancestor)

#Executes the program according to the provided mode
case command
when *commands_with_0_input
	puts wordnet.send(command)
when *commands_with_1_input 
	file = File.open(input_file)
	nouns = file.gets.split(/\s/)
	file.close    
    print_res(wordnet.send(command, nouns))
when *commands_with_2_input 
	file = File.open(input_file)   
	v = file.gets.split(/\s/).map(&:to_i)
	w = file.gets.split(/\s/).map(&:to_i)
	file.close
    print_res(wordnet.send(command, v, w))
when "root"
	file = File.open(input_file)
	v = file.gets.strip
	w = file.gets.strip
	file.close
    print_res(wordnet.send(command, v, w))
else
  fail "Invalid command"
end