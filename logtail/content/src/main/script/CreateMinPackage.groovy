def destFile = new File(project.build.directory, "${project.artifactId}-${project.version}-min.zip");
def sourceFile = new File(project.build.directory, "${project.build.finalName}.zip");

def buffer = new byte[1024];

def input = new java.util.zip.ZipInputStream(new FileInputStream(sourceFile));
def output = new java.util.zip.ZipOutputStream(new FileOutputStream(destFile));
 
def entry = input.getNextEntry();
while (entry != null) {
    def name = entry.getName();
        output.putNextEntry(new java.util.zip.ZipEntry(name));
        def length;
        while ((len = input.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }
    
    entry = input.getNextEntry();
}
input.close();
output.close();