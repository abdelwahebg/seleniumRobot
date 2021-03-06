### 1 Create a new test application ###
A "test application" is the code specific to the Web or Mobile application under test. It consists of testNG files, Cucumber feature files, configurations and Java implementation files.

Use seleniumRobot-example project as a base to develop your own test application
[https://github.com/bhecquet/seleniumRobot-example](https://github.com/bhecquet/seleniumRobot-example)

for the remainder, we use a unique name for that new application `appName`<br/>
**BE CAREFUL**: application name MUST NOT contain any `_` (underscore character)
- in pom.xml
	- remove all unnecessary plugins & configurations (see file comments)
	- change artifactId, groupId and version according to your organization
- change test package according to your organization. Last part of the package MUST BE `appName`
- change folder name under `data` to `appName`
- in `data/appName/testng/test_qwant.xml`, change value for `cucumberPackage` parameter according to the updated package name
- write a minimal test (or re-use the example)
- compile code with `mvn clean package`
- **WARNING Oracle JDBC**: when compiling test application code, dependency ojdbc6.jar is searched. If your test application does not use Oracle DB connection, exclude oracle artifact from core using 

	<dependency>
		<groupId>com.infotel.seleniumRobot</groupId>
		<artifactId>core</artifactId>
		<version>[3.9.1,)</version>
		<exclusions>
			<exclusion>
				<groupId>com.oracle</groupId>
				<artifactId>ojdbc6</artifactId>
			</exclusion>
		</exclusions>
	</dependency>
- else, install ojdbc via `mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0 -Dpackaging=jar -DcreateChecksum=true -Dfile=<path_to_ojdb6.jar>`
	
- execute it

#### Requirements are ####

- Test data are in `data/<appName>/`<br/>
	- `features` contains feature files (mandatory if cucumber mode is used)<br/>
	- `testng` contains testng files to start tests (mandatory)<br/>
	- `config` contains centralized env test configuration (optional)<br/>
	- `squash-ta` contains ta files that should override the default ones (optional). See Squash-TA section for details <br/>

- Test code must be in `src/test/java`
- **WARN** When using IntelliJ, you must also create a `src/main/java` with subpackage inside and a main class. This does nothing but IntelliJ cannot weave aspects without that: you get `Error:Module '<some_module>' tests: org.aspectj.bridge.AbortException: bad aspect library: 'D:\Dev\seleniumRobot-maaffr\target\classes'`
- package name is free but following structure MUST be used<br/>
	- `cucumber` subpackage contains cucumber implementation files. As cucumber annotations can be written directly in test page object, it should at least contain code for `@Then` (checks) and `@Given` (initial state).<br/>
If pure TestNG mode is used, this package MUST exist.<br/>
	- `tests` subpackage contains code for pure TestNG tests (not cucumber). If cucumber mode is used, this package should not exist<br/>
	- `webpage` subpackage is mandatory as it contains PageObject implementation<br/>

![](images/package_structure.png)

*If something goes wrong when launching test, check:*
- project name, folder name under `data`, sub-package name, containing `webpages` **SHOULD BE THE SAME**
- There is no space in folder structure
- In cucumber mode, the `@Given` creating the first PageObject must not be in the PageObject class. It should be in `cucumber`subpackage instead

Once done, configure your project with aspectJ compiler. See procedure for eclipse or IntelliJ in file [chap2_Installation.md](chap2_Installation.md)

#### Handle multiple version of the same test application ####
A SUT can be installed in several environement with different version: v3.0 in integration, v2.0 in acceptance tests, v1.0 in production
If you use the same seleniumrobot execution environment, you must be able to deploy several versions of the test application at the same time on this environment.
Therefore, do the following in pom.xml:
- Change `<finalName>${project.artifactId}</finalName>` by `<finalName>${project.artifactId}-${application.version.short}</finalName>`
- Change any execution of `maven-resources-plugin` containing `data`: <br/>
`<outputDirectory>${project.build.directory}/data/${project.artifactId}/config</outputDirectory>` by `<outputDirectory>${project.build.directory}/data/${project.artifactId}_${application.version.short}/config</outputDirectory>` <br/> 
`<outputDirectory>${project.build.directory}/data/${project.artifactId}/testng</outputDirectory>` by `<outputDirectory>${project.build.directory}/data/${project.artifactId}_${application.version.short}/testng</outputDirectory>` <br/> 
`<outputDirectory>${project.build.directory}/data/${project.artifactId}/features</outputDirectory>` by `<outputDirectory>${project.build.directory}/data/${project.artifactId}_${application.version.short}/features</outputDirectory>` <br/> 
- Add the plugin execution. It will be in charge of creating the `application.version.short` variable that we use above:

	<plugin>
		<groupId>org.codehaus.mojo</groupId>
		<artifactId>build-helper-maven-plugin</artifactId>
		<version>3.0.0</version>
		<executions>
			<execution>
				<id>regex-property</id>
				<goals>
					<goal>regex-property</goal>
				</goals>
				<configuration>
					<name>application.version.short</name>
					<value>${project.version}</value>
					<regex>^([0-9]+)\.([0-9]+)\.([0-9]+)(-SNAPSHOT)?$</regex>
					<replacement>$1.$2</replacement>
					<failIfNoMatch>true</failIfNoMatch>
				</configuration>
			</execution>
		</executions>
	</plugin>

#### Post-installation ####

Run `mvn clean package` so that a <app>-version.txt file can be automatically generated by maven
This is used by core when logging information

### 2 PageObject ###
PageObject is a design pattern that helps writing maintainable selenium applications. Each screen of the website or mobile application is a class.
This class contains:

- fields which are the elements on the page
	- links
	- buttons
	- text fields
	- ...
- methods which are the available actions in the page
	- validate form
	- fill form
	- access an other page

Example of a shopping cart class:

	public class ShoppingCart extends HeaderAndFooter {
	
		private static LinkElement proceed = new LinkElement("Checkout", By.linkText("Proceed to Checkout"));
		private static LinkElement updateCart = new LinkElement("updateCart", By.name("updateCartQuantities"));
		private static Table cart = new Table("cart", By.tagName("table"));
	
		public ShoppingCart() throws Exception {
			super(proceed);
		}
		
		public ShoppingCart changeQuantity(String item, String newQuantity) {
			new TextFieldElement("quantity", By.name(item)).sendKeys(newQuantity);
			updateCart.click();
			return this;
		}
		
		public String getTotalAmount() {
			String amount = cart.getContent(cart.getRowCount() - 1, 0).replace("Sub Total: $", "");
			return amount;
		}
		
		public SignIn checkout() throws Exception {
			proceed.click();
			return new SignIn(); 
		}
		
		public PaymentDetails checkoutSignedIn() throws Exception {
			proceed.click();
			return new PaymentDetails(); 
		} 
	}
	
**WARN:** If you write your class combined with cucumber feature (methods annotated with @Given, @When, ...), only write methods returning `void`. Else, report will contain new page create step twice.

#### Search elements ####

SeleniumRobot supports standard search through `By` class

	LinkElement proceed = new LinkElement("Checkout", By.linkText("Proceed to Checkout"));
	
Additional search using the `ByC` class are
- search element by attribute: `new TextFieldElement("", ByC.attribute("attr", "attribute")).sendKeys("element found by attribute");`. See: [https://developer.mozilla.org/en-US/docs/Web/CSS/Attribute_selectors] (https://developer.mozilla.org/en-US/docs/Web/CSS/Attribute_selectors) for special syntax like searching with attribute value starting by some pattern `ByC.attribute("attr^", "attributeStartPattern")`
- search element after a label: `new TextFieldElement("", ByC.labelForward("By id forward", "input")).sendKeys("element found by label");`
- search element before a label: `new TextFieldElement("", ByC.labelBackward("By id backward", "input")).sendKeys("element found by label backward");`
- search by first visible element: `new HtmlElement("", By.className("otherClass"), HtmlElement.FIRST_VISIBLE).getText()`
- search in reverse order (get the last element): `new TextFieldElement("", By.className("someClass"), -1);` get the last element on the list

When an element needs to be searched by several criteria to be uniquely identified, you can use the Selenium feature of chaining `By`. <br/>
E.g: `driver.findElement(By.tagName("h1").className("cls2"))` or  `new HtmlElement("", By.tagName("h1").className("cls2"))`

In case search is done with a ByC instance, this MUST be the placed first<br/>
E.g: `new HtmlElement("", ByC.attribute("attr", "attribute").className("cls2"))`

#### Add user defined test steps ####

By default, framework creates automatically test steps based on method calls. Every PageObject method called from a Test class is a step (for e.g)
If this behavior is not what you want, you can disable it by setting `manualTestSteps` to `true` as test parameters. 
Then, in Test class or PageObject sub-class, add `addStep("my step name")` where you want a step to be created. Every subsequent actions will be recorded to this step

For each step, a snapshot is done and step duration is computed

If, inside a step, you need to mask a password, (see: "masking password" section), give the value (generally, the variable name) to the `addStep` method

	addStep("my step name", myValueForPassword)

### 3 Write a test ###
A test is a suite of steps defined in several page objects. By convention, they are located in the `tests` folder
Assuming that the right objects are created, a test looks like:
    
    public class VmoeTests extends SeleniumTestPlan {
	
		@Test(
		        groups = { "vmoe" },
		        description = "check Angel Fish"
		    )
		public void consultProductDetails() throws Exception {
			ProductItem productItem = new JPetStoreHome(true)
				._goToFish()
				._accessAngelFish()
				._showItem("EST-1");
			Assert.assertEquals(productItem.getProductDetails().name, "Large Angelfish");
		}
	}

A typical PageObject method whould be

	public FishList _goToFish() throws Exception {
    	fishMenu.click();
    	return new FishList();
    }

**WARN** DO NOT give the same test name in different test classes as it leads to wrong logging reporting

#### Accessing test data ####
See part 6 ("env.ini configuration" and "seleniumRobot server") to see how to get test data in scenario

You may consider putting all test data access in the Test script, not in page object. This helps maintenance when we want to know which variables are used.

	@Test(groups={"recevabilite"})
	public void testSearch() throws Exception {
		new MireRH(true)
				._login(param("login"), param("password"))
				._arrivee();
	}
	
Test data are get using `param(<key>)` or updated via `createOrUpdateParam`. The later is only available when seleniumRobot server is used.
`createOrUpdateParam(<key>, <value>)` is used to store a variable with reference to environment, application and application version
`createOrUpdateParam(<key>, <value>, <boolean>)` is used to store a variable with reference to environment, application. Reference to application version is optional
	
**WARN** DO NOT try to access test data inside a `@BeforeXXX` method, they are not available. Only parameters defined in XML or as user parameters are available. At this stage, `env.ini` file or variable server have not been read.

#### Using TestNG annotations ####

As we use TestNG for test running, it's possible to use any TestNG annotation in your test class. See: [http://testng.org/doc/documentation-main.html] (http://testng.org/doc/documentation-main.html), section "2. annotation"<br/>
For more information on execution order of TestNG annotations, see [https://stackoverflow.com/questions/30587454/difference-between-beforeclass-and-beforetest-in-testng] (https://stackoverflow.com/questions/30587454/difference-between-beforeclass-and-beforetest-in-testng)

##### Test context #####

Test context is a set of technical information used to run the test (connect parameters, browser, ...). These information come from TestNG XML file and from user parameters (`DmyParam=myValue`)
It's managed by `SeleniumTestsContextManager` class which handles `SeleniumTestsContext` instances.

Only at start of test method, the context is completed with test data coming from `env.ini` file or from variable server.

**WARN** TestNG allows you to use `@BeforeTest`, `@BeforeMethod`, `@BeforeClass` annotated method to init the test. In these methods (as for `@AfterTest`, `@AfterClass`, `@AfterMethod`), you can access the test context using for example  `SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");`. So 3 state context are stored: 
- test context (the one fetched from TestNG XML file)
- class context
- method context

"Method context" inherits from "class context" if existing, which itself inherits from "test context"

- `@AfterTest` context is the same as the one after `@BeforeTest` call. Or it defaults to context from XML file
- `@AfterClass` context is the same as the one after `@BeforeClass` call, or if not present, the test context
- `@AfterMethod` context is the same as the one after `@Test` call

`@BeforeMethod` annotated methods MUST declare a `java.lang.reflect.Method` argument as first parameter to be usable (this is automatically injected by TestNG). Else, a ScenarioException is raised.
 
#### masking password ####

Most of tests needs to write password to application to authenticate, open some view, ...
By default, reports produced by SeleniumRobot display all actions, steps containing method calls with arguments. Then all password may be visible to anyone.
To avoid this potential security risk, name your method arguments containing a password with one of this 3 ways: `password`, `pwd`, `passwd`.
Any argument name containing one of these words will be masked.
e.g: `myPassWordLong` will also be masked as containing `password`

	Step _setPassword with args: (myPass, )
	sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [myPass,])
	
becomes

	Step _setPassword with args: (******, )
	sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [******,])
	
because method `_setPassword` signature is `public DriverTestPage _setPassword(String passWordShort) {`


**WARN: ** With manual steps, you have to explicitly give the passwords to mask when creating test steps 

	addStep("my step name", myValueForPassword)
	addStep("my step name", myValueForPassword1, myValueForPassword2)

#### TestNG file ####
For tests extending SeleniumTestPlan, the testNg XML looks like (minimal requirements):

	<suite name="Integration tests" parallel="false" verbose="1" thread-count="1">
	    <test name="order">	    	
	        <classes>
	            <class name="com.infotel.seleniumRobot.jpetstore.tests.VmoeTests">
	            	<methods>
	            		<include name="orderFish" />
	            	</methods>
	            </class>
	        </classes>
	    </test>
	</suite>
	
For more information on execution order of TestNG annotations, see [https://stackoverflow.com/questions/30587454/difference-between-beforeclass-and-beforetest-in-testng] (https://stackoverflow.com/questions/30587454/difference-between-beforeclass-and-beforetest-in-testng)

#### Access some path on disk ####

When you want to access a file on seleniumRobot path (for example, a file you put in data), you cas access relative path using `SeleniumTestsContextManager` static methods

- `SeleniumTestsContextManager.getApplicationDataPath()` => <seleniumRobot path>/data/<application>
- `SeleniumTestsContextManager.getConfigPath()` => <seleniumRobot path>/data/<application>/config
- `SeleniumTestsContextManager.getFeaturePath()` => <seleniumRobot path>/data/<application>/features
- `SeleniumTestsContextManager.getDataPath()` => <seleniumRobot path>/data/
- `SeleniumTestsContextManager.getRootPath()` => <seleniumRobot path>

### 4 Write a cucumber test ###
Cucumber styled tests rely on a `.feature` file where each test step is defined. Look at [https://cucumber.io/docs/reference](https://cucumber.io/docs/reference) for more information about writing a feature file.

Each line in the feature file must correspond to an implementation inside java code through annotation

	@When("Cliquer sur le lien 'FISH'")
    public void goToFish() throws Exception {
    	fishMenu.click();
    }

**WARN:**You should write only void methods to avoid getting twice the page creation in report
**WARN:**Java8 style (lambda expressions) is currently not supported by framework. Use only @Annotation style


#### Feature file example ####
	Feature: Catalogue
	
		Scenario: Consulter la fiche Angel Fish
			
			Given Ouvrir le jPetStore
			When Cliquer sur le lien 'FISH'
			And Cliquer sur le produit 'Angel Fish'
			And Cliquer sur le type 'EST-1'
			Then Le nom du produit est 'Large Angelfish'

#### TestNG file ####
XML testNg file looks like:

	<!DOCTYPE suite SYSTEM "http://beust.com/testng/testng-1.0.dtd" >
	<suite name="Integration tests" parallel="false" verbose="1" thread-count="1">
	
		<parameter name="cucumberPackage" value="com.infotel.seleniumRobot.jpetstore" />
	    
	    <test name="consult_catalog">
	    	<parameter name="cucumberTests" value="Consulter la fiche Angel Fish" />
		    <parameter name="cucumberTags" value="" />
	    	
	        <packages>
	            <package name="com.seleniumtests.core.runner.*"/>
	        </packages>
	    </test>
	   
	</suite>

`cucumberPackage` parameter is mandatory so that framework knows where implementation code resides. `cucumberTests` and `cucumberTags` help selecting the right scenario. See §4 for details

### 5 Working with frames ###
In case an HTML element has to be searched inside an iFrame there are 2 ways to handle this

#### Selenium way ####
Selenium offers the way to switch focus to an iframe using

	driver.switchTo().frame(<frameElement>)  // => switch to the iframe previously searched. Each search after this call will be done inside frame
	driver.switchTo().defautlContent()		 // => go back to the main page
	
The drawback of this approach is that if iframe reloads after switching, each element search will fail
Moreover, no retry is done when searching frame

#### SeleniumRobot way ####
SeleniumRobot adds a way to retry a search when an error occurs even using iframes

	// declare your frame as any other element inside page.
	FrameElement frame = new FrameElement("my frame", By.id("frameId"));
	
	// declare your element as being present inside this frame (the frame parameter)
	HtmlElement el = new HtmlElement("my element", By.id("el"), frame);
	
	// use the element
	el.click();
	
This way, each time an action is performed on the element, SeleniumRobot will:

- search the frame and switch to it
- act on element
- switch to default content

If an error occurs during one of these actions, SeleniumRobot will retry

### 6 Configure test scripts ###
There are several ways to make values in test script change according to test environment, executed test, ...

#### XML configuration ####
XML testing file handles many technical configurations: server address, used tools and related configuration.
 
Business configuration can be done through the `unknown` parameters. These are parameters which are not known from the framework. They are added to a list of business parameters.

#### env.ini configuration ####
XML configurations are done statically and must be duplicated through all the test suites (or using `testConfiguration` parameter). It's not possible to have a centralized configuration which depends on test environment.

*Example:* the server URL depends on testing phase. They are not the same in production and in integration phase.

That's why the `env.ini` file is made for. Each tested application can embed a env.ini file whose format is: 

![](images/config_ini_example.png)
 
`General` section is for common configuration (e.g: a database user name which does not depends on environment) and other sections are specific to named test environments. Here, we define a `Dev` environment. Then, when launching test, user MUST define the environment on which test will be run with the option `-Denv=Dev`

Keys defined in environment sections override the ones in `General` section.
This file must be located in "<<t>application root>/data/<<t>application name>/config" folder.

These configurations are also stored in the business configuration.

This file may be empty if seleniumRobot server is used

#### seleniumRobot server ####

Selenium robot server allows to store variables of all your test applications in a centralized place. See details in [chap6_Test_Manager_interfaces.md](chap6_Test_Manager_interfaces.md)

#### Using configurations (aka business configuration) in test scripts ####
Each webpage can use the configurations defined above using (getting variable `text` from configuration):

![](images/get_param_example.png)

#### Order of use ####

The business configuration are read in the following order (the last overwrites its predecessors)

- suite parameter (from TestNG XML file)
- test parameter (from TestNG XML file)
- system property (user defined value by -D<key>=<value> on command line)
- parameter defined in env.ini
- parameter defined in seleniumrobot server (if used)

### 7 Test script configuration mapping ###
#### Mapping files utility ####

Mapping file give possibility to call an element in a web page with a more accessible and understandable word. This way your code gain clarity for a non technical user.
Every element can be redefined for any platform and version, you just have to create a hierarchical architecture of files :
example :

![](images/mapping_architecture.png)

#### objectMapping.ini configuration ####

Each file can define new elements, and it inherits parents files.
The structure of an objectMapping.ini file looks like that :

![](images/objectMapping_example.png)

between [ ] you define the web page where to use the following definitions. Next, there is the word definitions : caller_word:technical_word

#### Mapping data use ####

In the corresponding pageObject you can use mapping words to search elements using : `locateBy(map:caller_word)` or `By.id(map:caller_word)`. It will search the element in the page which is defined by the technical word. 

### 8 Optional features ###
Here will be described features that may be used to improve test

#### Soft assertions ####
By default, inside a test, checks will be done using TestNG `Assert` class
On assert failure, test will continue but error will be reported
If this behaviour is not expected, then use the parameter `softAssertEnabled` and set it to false

#### Log some information ####
You can make seleniumrobot display some test information in logs

- `TestLogging.info("my message")`: displays a message (green in HTML report)
- `TestLogging.warning("my warn")`: displays a warning (orange in HTML report)
- `TestLogging.error("my error")`: displays an error (red in HTML report)
- `TestLogging.log("my log")`: displays the message without style
- `TestLogging.logTestValue("key", "my message", "my value")`: stores the key/value pair (displays a table in HTML report)


### 9 Write good tests and Page Objects ###

According to `http://www.slideshare.net/saucelabs/how-to-grade-your-selenium-tests-by-dave-haeffner-sauce-labs-webinar?mkt_tok=eyJpIjoiTlRFeVpUTXdNbVpoTlRNMiIsInQiOiI2UzdLYnBraTczaHU0cUM0Z1FcL2pxOWZEVFhPdWxRa2h0QjJXZFwvK1B2NXRXRnhpWVk4MlFmcGE5eE5Ub3lkUG40UXNES1JENzhHMmExREg4aG9wRTFMZHBSTGdFaWIyeWEzcXpXb1BvTHRVPSJ9`
you should follow these rules:
- each test is independant
- common parts are centralized (eased with PageObject)
- No explicit waits and sleeps: handled by this framework. At most configure proper timeouts
- Assertions are in tests, not in Page Object
- A Page Object contains locators (see example) and actions (the methods). They are seperated to simplify readability
- the locator use order is: id, name, className, cssPath, xPath, linkText because the first are most of time unique and do not change each time the DOM changes

### 10 Use Selenium code style inside SeleniumRobot ###

As PageObject exposes the `driver` object, it's possible to write standard Selenium code
	
	driver.findElement(By.id("myId")).click();
	
SeleniumRobot intercept selenium calls to create HtmlElement objects (the same as in §2) and thus benefit all SelniumRobot behaviour
The constraints are:
- this code *MUST* be placed in a PageObject sub-class.
- add `<parameter name="overrideSeleniumNativeAction" value="true" />` to your TestNG XML file 

This should be seen as a way to migrate legacy selenium code to the new format without rewriting every old test 

The handled methods are the most used in selenium writing:
- findElement(By)
- findElements(By)
- switchTo().frame(int)
- switchTo().frame(WebElement)
- switchTo().frame(String)
- switchTo().defaultContent()
- switchTo().parentFrame()
