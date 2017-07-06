# String-Resouce-Processor
Util for the string resources for the mobile developers

<pre>
java -jar str_processor-1.0.jar &lt;create|extract&gt; src dst [rtl] [ios]
</pre>

- extract - take the strings from the resource files
- create  - create the resource file(s) based on the text file
- src - file or folder with resources for the EXTRACT operation and file with texts for the CREATE operation
- dst - file for writing results, for the IOS-format the program creates additional file .strings for the CREATE operation
- rtl - it's flag for the languages with direction from the right to left. It's used for CREATE operation
- ios - it's flag IOS-format, without it here is being used android-format
