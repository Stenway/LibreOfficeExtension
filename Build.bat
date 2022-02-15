cls
rmdir /s /q build\

mkdir build\classes\
mkdir build\extension
mkdir build\extension\META-INF
javac -cp "C:\Program Files\LibreOffice\program\classes\libreoffice.jar" -d build\classes src\ImportExportFilter.java src\Utils.java src\ReliableTxt.java src\Wsv.java src\Sml.java src\Tbl.java src\WsvImporter.java src\TblImporter.java src\TblsImporter.java
jar cvfm build\extension\StenwayFormats.uno.jar src\StenwayFormats.uno.Manifest -C build\classes .
copy src\TypeDetection.xcu build\extension
copy src\StenwayFormats.components build\extension
copy src\manifest.xml build\extension\META-INF
del build\StenwayFormats.zip
del build\StenwayFormats.oxt
"C:\Program Files\7-Zip\7z.exe" a "build\StenwayFormats.zip" .\build\extension\*
rename "build\StenwayFormats.zip" StenwayFormats.oxt