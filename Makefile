SRC_DIR = src
BUILD_DIR = build
# MAIN_FILE = main.test
COMPILER_FLAGS = -g

# Find all .java files in src
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

all: compile_native
	# Create build directory if it doesn't exist
	mkdir -p $(BUILD_DIR)
	# Generate JNI header file for KeyPressReader
	javac -h $(SRC_DIR)/keyboard/C src/keyboard/KeyPressReader.java
	# Compile all Java files to the build directory
	javac $(COMPILER_FLAGS) -d $(BUILD_DIR) $(SOURCES)

run:
	# Run the main class with classpath set to the build directory
	java -Djava.library.path=$(SRC_DIR)/keyboard/C -cp $(BUILD_DIR) $(MAIN)

debug:
	# Start Java program in debug mode, waiting for debugger to attach on port 5005
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $(BUILD_DIR) $(MAIN)

debug_attach:
	# Attach jdb to the running process on port 5005
	jdb -attach 5005

clean:
	# Remove all compiled files in the build directory
	rm -rf $(BUILD_DIR)/*	

compile_native: 
	# Creating shared library for key_reader.c
	gcc -shared -o $(SRC_DIR)/keyboard/C/libkey_reader.so -fPIC \
	    -I/usr/lib/jvm/java-17-openjdk-amd64/include \
	    -I/usr/lib/jvm/java-17-openjdk-amd64/include/linux \
	    $(SRC_DIR)/keyboard/C/key_reader.c
