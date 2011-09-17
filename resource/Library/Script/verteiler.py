#!/usr/bin/env python
# encoding: utf-8
# port input input
# port input select
# port output out1
# port output out2
# port output out3
"""
untitled.py

Created by Sven Jäger on 2010-03-24.
Copyright (c) 2010 __MyCompanyName__. All rights reserved.
"""

# This is a template
class Main :
	"description here"
	def fire(self) :
		# Create ports, e.g. input and output
		# Read input, for example using
		token = self.input.get(0)
		s = self.select.get(0).doubleValue()
		# compute, and send an output using
		if s == 1:
			self.out1.broadcast(token)
		elif s == 2:
			self.out2.broadcast(token)
		elif s == 3:
			self.out3.broadcast(token)
