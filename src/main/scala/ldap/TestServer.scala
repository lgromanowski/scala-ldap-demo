package ldap

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig, InMemoryListenerConfig}

import scala.io.Source

object TestServer {
   var server: InMemoryDirectoryServer = null

   def start(): InMemoryDirectoryServer = {
     val config = new InMemoryDirectoryServerConfig("dc=example,dc=com")
     val listenerConfig = new InMemoryListenerConfig("test", null, 12345, null, null, null)
     config.setListenerConfigs(listenerConfig)
     config.setSchema(null) // do not check (attribute) schema
     server = new InMemoryDirectoryServer(config)
     server.startListening()

     val ldifFile = Source.fromURL(getClass.getResource("/admins.ldif"))
     val ldifLines = ldifFile.getLines.toSeq.filter(_.nonEmpty)
     server.add(ldifLines:_*)
     server
   }

   def stop(): Unit = {
     val closeExistingConnections = true
     server.shutDown(closeExistingConnections)
   }
 }
