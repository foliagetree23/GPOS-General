#!/bin/bash

# GPOS-General - Run Script
# This script runs the Java POS application

echo "======================================="
echo "  GPOS-General Runner  "
echo "======================================="

# Classpath with all required libraries
CP="bin:lib/flatlaf-3.7.jar:lib/flatlaf-extras-3.7.jar"

# Main class to run
MAIN_CLASS="com.pos.ui.MainPOS"

# Run the application
echo "Starting application..."
java -cp "$CP" "$MAIN_CLASS"

echo "======================================="
