name := "shopping-basket"

version := "1.0.0"

lazy val `shopping-basket` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  specs2                                                          % Test,
  "com.typesafe.akka"       %  "akka-testkit_2.12"    % "2.5.6"   % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play"   % "3.1.0"   % Test,
  "org.scalatest"           %% "scalatest"            % "3.0.4"   % Test
)
