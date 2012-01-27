import sbt._
import Process._
import com.twitter.sbt._

/**
 * Sbt project files are written in a DSL in scala.
 *
 * The % operator is just turning strings into maven dependency declarations, so lines like
 *     val example = "com.example" % "exampleland" % "1.0.3"
 * mean to add a dependency on exampleland version 1.0.3 from provider "com.example".
 */
class FranzProject(info: ProjectInfo) extends StandardServiceProject(info)
  with NoisyDependencies
  with IdeaProject
{
  override def dependencyPath = "lib"
  override def repositories = Set[Resolver]("exacttarget" at "http://etinjavbld1.et.local:8081/nexus/content/groups/public/")

  val finagleVersion = "1.9.0"

  val finagleCore = "com.twitter" % "finagle-core" % finagleVersion
  val ostrichExtas = "com.exacttarget" % "ostrich-extras" % "1.0.1"

  //kafka
  val log4j = "log4j" % "log4j" % "1.2.16"
  val zkClient = "com.github.sgroschupf" % "zkclient" % "0.1"

  val kestrel = "net.lag" % "kestrel" % "2.1.3"

  //json
  val lift_json = "net.liftweb" %% "lift-json" % "2.3"
  val lift_json_ext = "net.liftweb" %% "lift-json-ext" % "2.3"

  //val slf4jVersion = "1.5.11"
  //val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion withSources() intransitive()
  //val slf4jBindings = "org.slf4j" % "slf4j-jdk14" % slf4jVersion withSources() intransitive()

  // for tests
  val specs = "org.scala-tools.testing" % "specs_2.8.1" % "1.6.7" % "test" withSources()
  val jmock = "org.jmock" % "jmock" % "2.4.0" % "test"
  val hamcrest_all = "org.hamcrest" % "hamcrest-all" % "1.1" % "test"
  val cglib = "cglib" % "cglib" % "2.1_3" % "test"
  val asm = "asm" % "asm" % "1.5.3" % "test"
  val objenesis = "org.objenesis" % "objenesis" % "1.1" % "test"
  //var kafka = "org.linkedin" % "kafka" % "0.7.0-34e24b01"

  override def mainClass = Some("com.exacttarget.franz.Main")
}
