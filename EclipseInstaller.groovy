System.properties["http.proxyHost"]="some-proxy"
System.properties["http.proxyPort"]="8080"
System.properties["http.proxyUser"]="user1"
System.properties["http.proxyPassword"]="pass1"

Authenticator authenticator = new Authenticator() {    
public PasswordAuthentication getPasswordAuthentication() {
        return (new PasswordAuthentication("user1",
                "pass1".toCharArray()));
    }
};
Authenticator.setDefault(authenticator);
proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("some-proxy", 8080));

//def eclipseHome = System.console().readLine("\n\n> Enter your eclipse home directory: ")
ant = new AntBuilder()
def install_zip(url, file) {
    new File("zips").mkdirs()
    if (!new File("zips/"+file).exists()) {
        ant.get(src:url+file, dest: "zips")
    }
    ant.unzip(src: "zips/"+file, dest: "zips")
      ant.copy(todir: "features") {
         fileset(dir: "zips/features")
      }
      ant.copy(todir: "plugins") {
         fileset(dir: "zips/plugins")
      }
    
}
install_zip("http://sourceforge.net/projects/eclipse-tools/files/implementors/v0.0.16/", "dk.kamstruplinnet.implementors-0.0.16.zip")
install_zip("http://sourceforge.net/projects/moreunit/files/moreunit/Version%203.0.0.02/", "org.moreunit-3.0.0.02.zip")
install_zip("http://sourceforge.net/projects/eclemma/files/01_EclEmma_Releases/2.2.0/", "eclemma-2.2.0.zip")
install_zip("http://subclipse.tigris.org/files/documents/906/49260/", "site-1.8.18.zip")

def get_files(updateSite, kind) {
  URLConnection connection = new URL(updateSite+"/"+kind).openConnection(proxy)
  def html = connection.content.text
  def files = []
  html.eachLine {
        if (it.contains(".jar")) {
            files << it.substring(it.indexOf('.jar">')+6, it.indexOf('.jar</a>')+4)
        }
  }
  new File(kind).mkdirs()
  files.each {
      if (!new File(kind+"/"+it).exists()) {
        ant.get(src:updateSite+"/"+kind+"/"+it, dest: kind)
      }
  }
}
def install_update_site(updateSite) {
  get_files(updateSite, "features")
  get_files(updateSite, "plugins")
}

install_update_site("http://m2eclipse.sonatype.org/sites/m2e/0.12.1.20110112-1712/")
install_update_site("http://eclipse-fonts.googlecode.com/svn/trunk/FontsUpdate/")

def eclipse_copy_jar = { file, folder ->
      if (file.isFile() && file.name.endsWith(".jar") && file.parent.contains(folder)) {
         ant.copy(file: file.absolutePath, todir: "$eclipseHome/$folder")
         println "  Copied $file.name to $eclipseHome/$folder"
      }
}
def eclipse_copy_packageNamedFolder = { file, folder ->
   def isPackageNamedFolder = (file.name.split('\\.').length > 4)
   if (file.isDirectory() && isPackageNamedFolder && file.parent.endsWith("jars")) {
      ant.copy(todir: "$eclipseHome/$folder/$file.name") {
         fileset(dir: file.absolutePath)
      }
      println "  Copied $file.name to $eclipseHome/$folder"
   }
}

