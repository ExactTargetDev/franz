require 'rubygems'
require 'kestrel'

queue = Kestrel::Client.new('localhost:22133')
queue.set 'testWrite1', 'testing123'
print queue.get('testRead1')
