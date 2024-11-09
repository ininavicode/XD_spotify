SRC_DIR = src
BUILD_DIR = build
DOC_DIR = doc

# MAIN_FILE = main.test
COMPILER_FLAGS = -g
LIBS = lib/vlcj-natives-4.8.3.jar:lib/jna-platform-jpms-5.14.0.jar:lib/jna-jpms-5.14.0.jar:lib/jlayer-1.0.3.jar:lib/vlcj-4.8.4-SNAPSHOT.jar

# Find all .java files in src
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

JAVA_HOME ?= /usr/lib/jvm/java-17-openjdk-amd64


all: compile_native check_vlc
	# Create build directory if it doesn't exist
	mkdir -p $(BUILD_DIR)
	# Generate JNI header file for KeyPressReader
	javac -h $(SRC_DIR)/keyboard/C $(SRC_DIR)/keyboard/KeyPressReader.java
	# Compile all Java files to the build directory
	javac -cp $(LIBS) $(COMPILER_FLAGS) -d $(BUILD_DIR) $(SOURCES)


run:
	# Run the main class with classpath set to the build directory
	java -Djava.library.path=$(SRC_DIR)/keyboard/C -cp $(BUILD_DIR):$(LIBS) $(MAIN) $(IP) $(PORT)

debug:
	# Start Java program in debug mode, waiting for debugger to attach on port 5005
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -Djava.library.path=$(SRC_DIR)/keyboard/C -cp $(BUILD_DIR):$(LIBS) $(MAIN) $(IP) $(PORT)

debug_attach:
	# Attach jdb to the running process on port 5005
	jdb -attach 5005

clean:
	# Remove all compiled files in the build directory
	rm -rf $(BUILD_DIR)/*	

compile_native: 
	# In case of error, remember setting the JAVA_HOME variable at the Makefile, (instructions at README.md)
	# Creating shared library for key_reader.c
	gcc -shared -o $(SRC_DIR)/keyboard/C/libkey_reader.so -fPIC \
	    -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux \
	    $(SRC_DIR)/keyboard/C/key_reader.c

documentation:
	# Extract unique package names from the src directory structure
	PACKAGES=$$(find $(SRC_DIR) -type d | sed 's|$(SRC_DIR)/||' | tr '/' '.' | grep -v '^$$' | paste -sd ":" -); \
	echo "Generating Javadoc for packages: $$PACKAGES"; \
	# Generate documentation with dynamically found packages and include lib classpath
	javadoc -d $(DOC_DIR) -sourcepath $(SRC_DIR) -classpath $(LIBS) -subpackages "$$PACKAGES"

# Check for vlc package and prompt user if not installed
check_vlc:
	@command -v vlc >/dev/null 2>&1 || { \
		echo "VLC is not installed. Please install it with:"; \
		echo "    sudo apt update && sudo apt install vlc"; \
		exit 1; \
	}