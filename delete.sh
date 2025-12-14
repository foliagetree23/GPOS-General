#!/bin/bash

# --- Configuration ---
# 1. SET THE START DIRECTORY
#    (e.g., your project folder)
START_DIR="./"

# 2. SET THE FILE PATTERN TO DELETE
#    (e.g., "*.tmp", "*.log", "a.out", etc.)
FILE_PATTERN="*.class" 

# 3. SET PREVIEW MODE (Important Safety Step!)
#    Set to 'true' to see what *would* be deleted.
#    Set to 'false' to actually perform the deletion.
PREVIEW_MODE=false

# --- Script Logic ---

echo "Starting recursive cleanup in: $START_DIR"
echo "-------------------------------------"

if $PREVIEW_MODE; then
    echo "!!! PREVIEW MODE IS ACTIVE. Nothing will be deleted. !!!"
    # Find files and print them
    echo "Files that WOULD be deleted (matching '$FILE_PATTERN'):"
    find "$START_DIR" -type f -name "$FILE_PATTERN" -print

    # Find directories that WOULD be deleted (if empty)
    echo ""
    echo "Directories that WOULD be deleted (if empty after file deletion):"
    find "$START_DIR" -type d -empty -print
    
else
    echo "!!! DELETION MODE IS ACTIVE. Files will be permanently removed. !!!"
    
    # 1. Delete matching files
    echo "Deleting files matching '$FILE_PATTERN'..."
    find "$START_DIR" -type f -name "$FILE_PATTERN" -delete
    
    # 2. Delete empty directories (recursively cleans up tree structure)
    echo "Cleaning up empty directories..."
    # We use multiple passes to ensure directories that become empty after their
    # subdirectories are deleted are also removed.
    for i in {1..3}; do
        find "$START_DIR" -type d -empty -delete
    done
    
    echo "Cleanup complete."
fi

echo "-------------------------------------"
