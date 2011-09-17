#!/usr/bin/env python
# encoding: utf-8
# port output select
# port input in1
# port input in2
# port input in3
"""
untitled.py

Created by Sven Jäger on 2010-03-24.
Copyright (c) 2010 __MyCompanyName__. All rights reserved.
"""
from ptolemy.data import IntToken

# This is a template
class Main :
	"description here"
	def fire(self) :
		# Create ports, e.g. input and output
		# Read input, for example using
		# compute, and send an output using
		if self.in1.hasToken(0):
			self.in1.get(0)
			self.select.broadcast(IntToken(1))
		if self.in2.hasToken(0):
			self.in2.get(0)
			self.select.broadcast(IntToken(2))
		if self.in3.hasToken(0):
			self.in3.get(0)
			self.select.broadcast(IntToken(3))
