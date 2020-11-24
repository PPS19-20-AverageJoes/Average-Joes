

lazy val root = (project in file("."))
  .settings(

    scalaVersion := "2.12.9",

    mainClass in (Compile, packageBin) := Some("averageJoes.view.View"),

    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % Test,

      "com.typesafe.akka"    %% "akka-actor-typed"            % "2.6.9",
      "com.typesafe.akka"    %% "akka-actor"                  % "2.6.9",
      "com.typesafe.akka"    %% "akka-testkit"                % "2.6.9"      % "test",
      "com.typesafe.akka"    %% "akka-actor-testkit-typed"    % "2.6.9"      % "it,test",

      "org.scalactic"        %% "scalactic"                   % "3.2.0",
      "org.scalacheck"       %% "scalacheck"                  % "1.14.0"     % "it,test",
      "org.scalatest"        %% "scalatest"                   % "3.0.6"      % "test",
      "org.scalatest"        %% "scalatest-wordspec"          % "3.2.0"      % Test,

      "org.scalatestplus"    %% "scalacheck-1-14"             % "3.1.0.1"    % "it,test",
     // "org.scalatestplus"    %% "scalatestplus-mockito"       % "1.0.0-M2"   % "it,test",
     // "org.scalatestplus"    %% "mockito-3-4"                 % "3.2.2.0"    % "test",

      "org.mockito"          %% "mockito-scala"               % "1.10.2"     % "it,test",

      "com.typesafe.akka"    %% "akka-cluster-tools"          % "2.6.9",
      "com.typesafe.akka"    %% "akka-cluster-typed"          % "2.6.9",

      "org.slf4j"              % "slf4j-simple"                 % "1.7.30" ,

      "org.scala-lang.modules" %% "scala-xml"                   % "1.1.1"  ,
      "org.scala-lang.modules" %% "scala-parser-combinators"    % "1.1.1"  ,
      "org.scala-lang.modules" %% "scala-swing"                 % "2.0.3"  ,


      "net.liftweb" %% "lift-json" % "3.4.2",
      "com.typesafe.play" %% "play-json" % "2.9.1"

),
    // Used for sbt assembly
    //resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),

    fork in run := true,
    crossPaths := false,
    Test / parallelExecution := false,
  )

/*
sbt assembly
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
*/

/* To be copied to project/plugins.sbt */
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")



