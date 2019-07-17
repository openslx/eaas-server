package de.bwl.bwfla.emucomp.components.network;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.net.ConfigKey;
import de.bwl.bwfla.common.utils.net.PortRangeProvider;
import de.bwl.bwfla.emucomp.api.ComponentConfiguration;
import de.bwl.bwfla.emucomp.api.NodeTcpConfiguration;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.control.connectors.EthernetConnector;
import de.bwl.bwfla.emucomp.control.connectors.InfoDummyConnector;
import org.apache.tamaya.inject.api.Config;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

public class NodeTcpBean extends EaasComponentBean {

    protected DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
    private ArrayList<DeprecatedProcessRunner> vdeProcesses = new ArrayList<DeprecatedProcessRunner>();

    @Inject
    @ConfigKey("components.tcpNode.ports")
    private PortRangeProvider.Port tcpPorts;

    @Inject
    @Config("components.binary.nodetcprunner")
    private String nodeTcpRunner;

    @Inject
    @Config("components.binary.nodetcpscript")
    private String nodeTcpScript;

    @Override
    public void destroy() {
        tcpPorts.release();
        for (DeprecatedProcessRunner process : this.vdeProcesses) {
            process.stop();
            process.cleanup();
        }
        super.destroy();
    }

    @Override
    public void initialize(ComponentConfiguration config) throws BWFLAException {

        NodeTcpConfiguration nodeConfig = (NodeTcpConfiguration) config;



        String hwAddress = nodeConfig.getHwAddress();
        String switchName = "nic_" + hwAddress;

        int extPort;
        try {
             extPort = tcpPorts.get();
             System.out.println("connection on port: " + extPort);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BWFLAException(e);
        }

        DeprecatedProcessRunner process = new DeprecatedProcessRunner("vde_switch");
        process.addArgument("-hub");
        process.addArgument("-s");
        process.addArgument(this.getWorkingDir().resolve(switchName).toString());
        if(!process.start())
            throw new BWFLAException("Cannot create vde_switch hub for VdeSlirpBean");
        vdeProcesses.add(process);

        runner.setCommand(nodeTcpRunner);
        String info = null;
        if(nodeConfig.isDhcp())     // DCHCPD hack
        {
            // Usage: ./eaas-proxy "" /tmp/switch1 "" 10.0.0.1/24 dhcpd
            runner.addArgument("");
            runner.addArgument(this.getWorkingDir().resolve(switchName).toString());
            runner.addArgument("");
            runner.addArgument(nodeConfig.getPrivateNetIp() + "/" + nodeConfig.getPrivateNetMask());
            runner.addArgument("dhcpd");
        }
        else {
            // arg1 extPort
            // arg2 wsURL
            // arg3 randomMac
            // arg4 privateNetworkIp (internal)/24
            // arg5 privateDestIp (internal server)
            // arg6 privateDestIpPort
            runner.addArgument(extPort + "");
            runner.addArgument(this.getWorkingDir().resolve(switchName).toString());
            runner.addArgument(NetworkUtils.getRandomHWAddress());
            runner.addArgument("dhcp");
            // runner.addArgument(nodeConfig.getPrivateNetIp() + "/" + nodeConfig.getPrivateNetMask());

            if (nodeConfig.isSocksMode()) {
                String sockString = "socks5";
                info = "socks/" + extPort;
                if (nodeConfig.getSocksUser() != null && nodeConfig.getSocksPasswd() != null) {
                    sockString += ":" + nodeConfig.getSocksUser() + ":" + nodeConfig.getSocksPasswd();
                    info += "/" + nodeConfig.getSocksUser() + "/" + nodeConfig.getSocksPasswd();
                }
                runner.addArgument(sockString);
            } else if (nodeConfig.getDestIp() != null && nodeConfig.getDestPort() != null) {
                runner.addArgument(nodeConfig.getDestIp());
                runner.addArgument(nodeConfig.getDestPort());
                info = "tcp/" + extPort;
            } else
                throw new BWFLAException("invalid node tcp config.");
            this.addControlConnector(new InfoDummyConnector(info));
        }

        if (!runner.start())
            throw new BWFLAException("Cannot start node process");
        vdeProcesses.add(runner);

        this.addControlConnector(new EthernetConnector(hwAddress, this.getWorkingDir().resolve(switchName)));
    }

    @Override
    public String getComponentType() throws BWFLAException {
        return "nodetcp";
    }

    public static NodeTcpBean createNodeTcp(NodeTcpConfiguration config) throws ClassNotFoundException {
        String targetBean = "NodeTcpBean";

        Class<?> beanClass = Class.forName(NodeTcpBean.class.getPackage().getName() + "." + targetBean);
        return (NodeTcpBean) CDI.current().select(beanClass).get();
    }
}
