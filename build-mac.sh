#!/bin/bash

# GPOS-General - Build Script for macOS
# This script compiles the Java POS application on macOS

echo "========================================"
echo "  GPOS-General Builder (macOS)"
echo "========================================"

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "Error: Java JDK is not installed or not in PATH"
    echo "Please install Java JDK 8 or higher from:"
    echo "https://www.oracle.com/java/technologies/javase-downloads.html"
    echo ""
    echo "Or install via Homebrew:"
    echo "  brew install openjdk@17"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(javac -version 2>&1 | awk '{print $2}')
echo "Java Version: $JAVA_VERSION"

# Create bin directory if it doesn't exist
if [ ! -d "bin" ]; then
    mkdir bin
    echo "Created bin directory"
fi

# Compile all Java files
echo "Compiling Java source files..."

# Find all Java files and compile them
find src -name "*.java" > sources.txt
javac -d bin -cp src @sources.txt

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"

    # Count compiled class files
    CLASS_COUNT=$(find bin -name "*.class" | wc -l)
    echo "Generated $CLASS_COUNT class files"

    echo ""
    echo "To run the application:"
    echo "  java -cp bin com.pos.ui.MainPOS"
    echo ""
    echo "To create a JAR file:"
    echo "  echo 'Main-Class: com.pos.ui.MainPOS' > manifest.txt"
    echo "  jar cfm GeneralPOS.jar manifest.txt -C bin ."
    echo "  java -jar GeneralPOS.jar"

else
    echo "✗ Compilation failed!"
    echo "Please check the error messages above"
    exit 1
fi

# Clean up
rm -f sources.txt

echo ""
echo "Build completed successfully!"
echo "========================================"
