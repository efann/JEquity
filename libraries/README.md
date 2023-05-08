## Drivers Library

Do not upgrade the Apache Derby driver files. The new verions cause issues.

So, for the time being, we are remaining with version 10.14.2

Starting with 10.15.1.3, a new feature was added as described below:

From https://db.apache.org/derby/releases/release-10.15.1.3.html 

**JPMS modularization** - Derby has been re-packaged as a set of JPMS modules. This introduced a new jar file, derbyshared.jar, required by all configurations. Module diagrams for Derby configurations can be found in the javadoc for the 10.15 public API.

## NSMenuFX Library

We have removed the javafx*11.jar files as those are provided by the JDK that we are using, which is currently the BellSoft JDK 19

