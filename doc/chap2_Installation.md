### 1 Development environment ###

#### eclipse ####
SeleniumRobot is developed using eclipse IDE. Following plugins are mandatory:

- aspectj plugin (AJDT): use the dev version are older versions are not compatible with recent eclipse versions
- m2e plugin (maven)
- subeclipse (if using SVN for test application)
- TestNG (execute testNG tests)

SeleniumRobot needs Java 8 to be compiled.

It has also been tested with IntelliJ, however this requires the AspectJ compiler plugin which is not available in community version. A solution may be to compile seleniumRobot using maven.

Sometimes, eclipse does not get the same environment variables than the system. This can block mobile testing when it search for `node` installed on system. Simply add the path to node installation to `PATH` environment variable (e.g: `$PATH:/usr/local/bin`)

##### Test application project configuration #####
When developping test application on eclipse, `core` aspects must be woven by IDE when tests are launched from it. 
Convert project to aspectJ project:
Right click on project -> Configure -> convert as AspectJ project

Therefore, right click on test application project -> properties -> Build path -> AspectJ Build -> Aspect Path.
Add either the `core` project if you have it in eclipse or add the `core-x.y.z.jar` file. 

#### intelliJ ####
IntelliJ Ultimate can be used to develop test applications (or seleniumrobot project itself) but requires some configuration to activate AspectJ

##### AspectJ activation #####
This step must be done only once to make IntelliJ work with aspectJ
- follow this guide to configure AspectJ for IntelliJ: [https://www.jetbrains.com/help/idea/aspectj.html](https://www.jetbrains.com/help/idea/aspectj.html)
- File -> Settings -> Plugins: add aspectJ plugins (search aspectJ in search box and tick all)

##### Test Application Project configuration #####
- Import the project (from eclipse or from maven)
- By default, project is not seen as a maven one if imported from eclipse, so, right click on project -> add framework -> select maven
- File -> Settings -> Build -> Compiler -> Java Compiler: <br/>
	- select Ajc compiler<br/>
	- configure path to aspectj compiler (aspectjtools.jar should be available somewhere if aspectJ activation has been done correctly)
	- choose the correct project bytecode version<br/>
	- add module and choose the correct per module bytecode version<br/>
	- select "delegate to javac"<br/>
- File -> Settings -> Build -> Required plugins: add "AspectJ support"
- File -> Project Structure -> Project: select the right SDK version and language level
- File -> Project Structure -> Modules: for the existing module<br/>
	- source tab: select the right language level<br/>
	- dependencies tab: remove eclipse specific dependencies (if project was previously imported from eclipse)<br/>
- File -> Project Structure -> Facects: 
	- add aspectj for the current module <br/>
	- tick "post-compile weave mode"<br/>
	- add seleniumRobot:core dependency in aspect path (in case of a test application. If developing core, aspect path are selenium-api and remode-remote-driver)<br/>
	
##### Version control #####
IntelliJ generate a lot of files. If you want to share the project through version control, some files may be excluded.
See 
[https://intellij-support.jetbrains.com/hc/en-us/articles/206544839-How-to-manage-projects-under-Version-Control-Systems](https://intellij-support.jetbrains.com/hc/en-us/articles/206544839-How-to-manage-projects-under-Version-Control-Systems)

As maven project, I remove `.idea/workspace.xml`, and all XML files under `.idea/libraries` 

#### Sign artifact for deploy phase ####
OSS needs artifacts to be signed before being deployed
Therefore, generated a GPG key (it will ask a name and a password)

	gpg --gen-key

Copy key to a public key server

	gpg2 --keyserver hkp://pgp.mit.edu --send-key <key_name>

#### Git key for release ####
In order for maven to push tags on release, a key must be generated for SSH connection

    ssh-keygen -t rsa -C '<key name>'
Copy the generated public key to Github
Check connection (it should reply: You've successfuly authenticated)

    ssh git@github.com

#### Maven configuration ####
Maven 3 is required

Following configurations should be placed in user `settings.xml` file:

For GPG

	<profile>
		<id>gpg</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.passphrase><your key password></gpg.passphrase>
      </properties>
    </profile>

For Sonar analysis

	<profile>
		<id>sonar</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<properties>
			<sonar.host.url>http://server:9000</sonar.host.url>
		</properties>
	</profile>

For publishing artifacts to OSS Sonatype server

	<server>
      <id>ossrh</id>
      <username><your_user></username>
      <password><your_password></password>
    </server>
    
#### Oracle library for core compile ####

In order to compile core artifact, you must provide the ojdb6.jar file into src/lib folder so that it can be automatically installed in maven local repository when doing `mvn clean`
This is not necessary when developping only test applications

#### Download drivers ####
From seleniumRobot 3.3.0, drivers are not provided anymore with source code, they are downloaded by a maven build (driver-download module)
To setup environment, execute `mvn clean compile` on project `driver-download`
If you are behind a corporate proxy, you should already have your settings.xml configured to use it.
/!\ you *MUST* have a configuration for http *and* https proxy. Else, download won't be possible


### 2 Execution environment ###
Execution environment needs at least Java 8. SeleniumRobot is compatible with Windows, Mac OS and Linux.
Depending on your tests, you should consider install:

- A browser 
- Appium
- Android SDK / Genymotion to test on android simulator / emulator
- XCode (Mac OS X) to test on iPhone Simulator 

#### Installing Appium ####
Either install it with .dmg on Mac or .exe on Windows or through npm `npm install -g appium`
Set `APPIUM_HOME` environment variable to point to path where appium has been installed:
- On Windows, using .exe, it's the root path where Appium.exe is located
- On Mac, using .dmg, it will be `/Applications/Appium.app/Contents/Resources/app`
- On any platform using npm installation, it will be the path where root `node_modules` folder has been created. This folder should contain an `appium` subfolder

On Linux/Mac systems, you can add `export APPIUM_HOME=<path to appium>` to the `.bash_profile` file in home directory

When running seleniumRobot from eclipse, it may not inherit the user environment variables, so set it in Run Configuration

#### Configuring Android for tests ####
- Install android SDK (the zip/command line tools version is enough) : [https://developer.android.com/studio/index.html#downloads](https://developer.android.com/studio/index.html#downloads)
- Install Intel HAXM driver to allow Virtual machine speedup. This can also be installed through Android SDK Manager
- Open SDK Manager and select the android images corresponding to the versions you wish to use (select only x86 Atom 64 bits). Install the components
- In AVD Manager, create a virtual machine (activate graphic acceleration) and start it. You should be able to use your Android virtual device.
- Add `ANDROID_HOME` environment variable pointing to root of android tools. This is the directory where AVDManager is copied

#### Configuring iOS for tests ####
Follow appium instruction here [http://appium.io/slate/en/master/?ruby#running-appium-on-mac-os-x] (http://appium.io/slate/en/master/?ruby#running-appium-on-mac-os-x)
As of June 2017, it asks for (simulator and real device):
- install xcode
- install xcode-select
- `npm install -g authorize-ios`
- `sudo authorize-ios`
- install homebrew (see homebrew website)
- `brew install carthage`

Check installation with appium-doctor:
- `npm install appium-doctor`
- in node_modules/appium-doctor, `node . --ios`

For use with a real iOS device, follow instructions here: 
[http://appium.io/slate/en/master/?ruby#appium-on-real-ios-devices] (http://appium.io/slate/en/master/?ruby#appium-on-real-ios-devices)
[https://github.com/appium/appium-xcuitest-driver#real-devices] (https://github.com/appium/appium-xcuitest-driver#real-devices)
[https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md] (https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md)
For real device only:
- `brew install libimobiledevice`
- `npm install -g ios-deploy`

An Apple Developper account is also mandatory. Add your real devices to list of devices and assign it to a iOS development profile

Automatic configuration should be enough, but give a try to manual configuration if something goes wrong

 