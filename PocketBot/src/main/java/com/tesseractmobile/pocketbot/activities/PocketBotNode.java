package com.tesseractmobile.pocketbot.activities;

import java.net.URI;

import com.tesseractmobile.pocketbot.robot.BaseRobot;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;

import org.ros.address.InetAddressFactory;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import geometry_msgs.Twist;

/**
 * Created by josh on 8/1/16.
 *
 * Creates nodes the publish information received by sensor data to ROS
 */
public class PocketBotNode implements NodeMain {

    public PocketBotNode(final NodeMainExecutor nodeMainExecutor, final URI masterUri) {
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        masterUri);
        nodeMainExecutor
                .execute(this, nodeConfiguration.setNodeName("pocketbot"));
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        initFacePublisher(connectedNode);
        initTeleopPublisher(connectedNode);
    }

    private void initFacePublisher(final ConnectedNode connectedNode) {
        final Publisher<Object> publisher = connectedNode.newPublisher("~face", Twist._TYPE);
        final Twist msg = (Twist) publisher.newMessage();
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(final SensorData sensorData) {
                msg.getLinear().setX(sensorData.getFace().X);
                msg.getLinear().setY(sensorData.getFace().Y);
                msg.getLinear().setZ(sensorData.getFace().Z);
                publisher.publish(msg);
            }
        });
    }

    private void initTeleopPublisher(ConnectedNode connectedNode) {

        final Publisher<Object> teleopPublisher = connectedNode.newPublisher("~cmd_vel", Twist._TYPE);
        final Twist msg = (Twist) teleopPublisher.newMessage();
        Robot.get().registerSensorListener(new BaseRobot.SensorListener() {
            @Override
            public void onSensorUpdate(final SensorData sensorData) {
                msg.getAngular().setZ(-sensorData.getControl().joy1.X);
                msg.getLinear().setX(sensorData.getControl().joy1.Y);
                teleopPublisher.publish(msg);
            }
        });
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}
