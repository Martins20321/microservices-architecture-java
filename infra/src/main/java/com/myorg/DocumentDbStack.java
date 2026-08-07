package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.docdb.DatabaseCluster;
import software.amazon.awscdk.services.docdb.Login;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.Collections;

public class DocumentDbStack extends Stack {
    public DocumentDbStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public DocumentDbStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        CfnParameter dbPassword = CfnParameter.Builder.create(this, "dbPasswordDocdb")
                .type("String")
                .description("Senha do banco usando CFN Parameter")
                .noEcho(true)
                .build();

        SecurityGroup sgDocumentDb = SecurityGroup.Builder.create(this, "DocumentDbSecurityGroup")
                .securityGroupName("pagamanetos-ms-docdb-sg")
                .description("Security Group para clustêr do DocumentDb")
                .vpc(vpc)
                .build();
        sgDocumentDb.addIngressRule(Peer.anyIpv4(), Port.tcp(27017));

        DatabaseCluster cluster = DatabaseCluster.Builder.create(this, "Database")
                .masterUser(Login.builder()
                        .username("pagamentos_admin") // NOTE: 'admin' is reserved by DocumentDB
                        .password(SecretValue.unsafePlainText(dbPassword.getValueAsString()))
                        .build())
                .instanceIdentifierBase("pagamentos-docdb") //Prefixo das instâncias do cluster
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
                .instances(1)  //Ele cria automaticamente uma instância primária (gravação e leitura)
                .vpc(vpc)
                .securityGroup(sgDocumentDb)
                .vpcSubnets(SubnetSelection.builder()
                        .subnets(vpc.getPrivateSubnets())
                        .build())
                .copyTagsToSnapshot(true)
                .build();

        CfnOutput.Builder.create(this, "pagamentos-db-endpoint")
                .exportName("pagamentos-db-endpoint")
                .value(cluster.getClusterEndpoint().getHostname())
                .build();

        CfnOutput.Builder.create(this, "pagamentos-db-password")
                .exportName("pagamentos-db-password")
                .value(dbPassword.getValueAsString())
                .build();
    }
}
