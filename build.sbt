
lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.11",
    // javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % Test,
      //"com.novocode" % "junit-interface" % "0.11" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      //"org.seleniumhq.selenium" % "selenium-java" % "2.35.0" % Test,
      //"io.cucumber" %% "cucumber-scala" % "2.0.1" % Test,
      //"io.cucumber" % "cucumber-junit" % "2.4.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
    ),
    crossPaths := false, // https://github.com/sbt/junit-interface/issues/35
    Test / parallelExecution := false
  )

// Cucumber configuration
// Run by:  sbt> cucumber
//enablePlugins(CucumberPlugin)

//CucumberPlugin.glue := "testLecture/code/steps"