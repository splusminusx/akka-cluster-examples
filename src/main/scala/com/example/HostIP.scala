package com.example

import scala.collection.JavaConversions._
import java.net.NetworkInterface

object HostIP {

  /**
   * @return the ip address if it's a local address
    *         (172.16.xxx.xxx, 172.31.xxx.xxx , 192.168.xxx.xxx, 10.xxx.xxx.xxx)
   */
  def load(): Option[String] = {
    val interfaces = NetworkInterface.getNetworkInterfaces
    val interface = interfaces find (_.getName equals "eth0")

    interface flatMap { inet =>
      inet.getInetAddresses find (_.isSiteLocalAddress) map (_.getHostAddress)
    }
  }
}