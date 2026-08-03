package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class InfraApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpcStack = new VpcStack(app, "MsVpc");
        ClusterStack clusterStack = new ClusterStack(app, "MsCluster", vpcStack.getVpc());
        clusterStack.addStackDependency(vpcStack);
        RdsStack rdsStack = new RdsStack(app, "MsRdsPedidos", vpcStack.getVpc());
        rdsStack.addStackDependency(vpcStack);

        app.synth();
    }
}

