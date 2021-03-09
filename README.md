# Demo Azure Spring Cloud Api

## Components:

*	Azure Spring Cloud application hosting a Spring Boot application configured by JHipster
*	Azure SQL Server
*	Azure SQL database
*	Azure Key Vault

Demo Spring Boot app is created and by [JHipster initializer](https://start.jhipster.tech/) _(currently only maven is supported for automatic deployment of Spring Boot apps to Azure Spring Cloud)_

The app needs to have a connection to the _Azure SQL DB_. For testing purposes allow access to your IP address in Firewalls and virtual networks section of the Azure SQL Server resource.


## Deploying to Azure Spring Cloud

The command for deploying a Spring Boot app to Azure Spring Cloud using JHipster is:

    jhipster azure-spring-cloud

Before executing the command the spring-cloud resource and resource group need to be configured because the _jhipster_ command does not support parameters

    az configure --defaults spring-cloud=demo-azure-spring-cloud-api
    az configure --defaults group=demo-azure-spring-cloud-api

This command will add a couple of azure related spring dependencies, an azure profile in **pom.xml** and specific azure configuration files in the resources folder.


## Adding Always Encrypted

[Azure documentation](https://docs.microsoft.com/en-us/sql/relational-databases/security/encryption/always-encrypted-database-engine?view=sql-server-ver15#how-it-works)

### 1. Configure Always encrypted

*   Enable client application access by creating an _Azure Active Directory_ application (**AAD**) which enables the Spring Boot app to authenticate itself in order to retrieve the _Column Master Key_ (**CMK**) for encrypting/decrypting the data. See [how to register an application](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#register-an-application-with-azure-ad-and-create-a-service-principal)
*   Create an application secret for the AAD and save its value for the actual authentication. Check option 2 for [authentication](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#authentication-two-options )
*   Add privileges for the AAD application to the _Azure Key Vault_
    
    `az keyvault set-policy --name $vaultName --key-permissions get list sign unwrapKey verify wrapKey --resource-group $resourceGroupName --spn $applicationId`
    
    or Directly from the portal under Access Policies of the Key Vault resource 
*   Install SQL Server Management Studio and connect to the database 
*   Right click on the table you want to encrypt the data and select _Encrypt Columns…_
*   Follow the [instructions](https://docs.microsoft.com/en-us/sql/relational-databases/security/encryption/always-encrypted-database-engine?view=sql-server-ver15#getting-started-with-always-encrypted) to use _Azure Key Vault_ for saving the CMK

### 2. Configure the Spring Boot application to use Always Encrypt for manipulating with encrypted data
*	Change the JDBC connection to support Always Encrypted – the driver itself will check the database metadata to see which column is encrypted, get the Column Encryption Key, go to the Key Vault to get the Column Master Key, decrypt the data and return it to the end user.
*	JDBC connections for drivers v 7.4.1 or later:

    jdbc:sqlserver://<server>:<port>;columnEncryptionSetting=Enabled;keyStoreAuthentication=KeyVaultManagedIdentity

    jdbc:sqlserver://<server>:<port>;columnEncryptionSetting=Enabled;keyStoreAuthentication=KeyVaultManagedIdentity;keyStorePrincipal=<AAD Application Object ID>

    jdbc:sqlserver://<server>:<port>;columnEncryptionSetting=Enabled;keyStoreAuthentication=KeyVaultClientSecret;keyStorePrincipalId=<AAD Application Client ID>;keyStoreSecret=<AAD application secret value>

#### Useful commands:
    az spring-cloud app logs -f –name <azure-spring-cloud-app-name>

### Infrastructure Diagram

![Diagram](src/main/resources/static/images/Diagram.png?raw=true)


# Running a Spring Boot app in a virtual network

## Components:

* Virtual Network
* Azure Spring Cloud application hosting a Spring Boot in the virtual network
* Azure SQL Server
* Azure SQL database
* Azure Key Vault
* Application Gateway
* Public IP Address
* Managed Identity
* Private DNS Zone


### 1. Deploying an application in a Virtual Network

*   Create a _Virtual Network_ and _Azure Spring Cloud_ by following the [instructions](https://docs.microsoft.com/en-us/azure/spring-cloud/spring-cloud-tutorial-deploy-in-azure-virtual-network#prerequisites)
*   Deploy your Spring Application same as described above
*   In order to access the application deployed in a virtual network you need to create an _Azure Private DNS Zone_, link it to the _Virtual Network_ and create _DNS records_ in this zone for the service-runtime-subnet [Instructions](https://github.com/microsoft/vnet-in-azure-spring-cloud/blob/master/03-access-your-application-in-private-network.md#access-your-application-in-private-network)

### 2. Expose the Application to the Internet

In order to expose the application created in a private network to the internet we need an Application Gateway
*	Create a subnet in the same _Virtual Network_ for the _Application Gateway_
*	Create a _Public IP Address_ which will be used as the Frontend of the Application Gateway. While creating the resource select Static allocation method and Standard SKU
*	Create a _Managed Identity_ resource and give it access to the Key Vault by adding it to the Access Policies
*	Follow the step-by-step instructions to create an _Application Gateway_ resource 
     * In the first step provide the Subscription and Resource group where the vnet resides, give a name and select the Virtual Network and the newly created subnet for the gateway.
     * In the second step select the Public IP Address that you have created
     * In the third step add a backend pool using the fully qualified domain name (**FQDN**) of the endpoint we have assigned to the app running in Azure Spring Cloud
     * In the Configuration add a routing rule connecting the frontend IP with the backend pool.
       
       On the Listener page:
        - Listener name: [your listener name]
        - Frontend IP: [the created Public IP Address]
        - Protocol: HTTPS
        - Certificate: Choose certificate from the Key Vault
        - Managed Identity: [select the new managed identity]
        - Key Vault: [your key vault]
        - Certificate: [your certificate]
       On the Backend Targets page:
        - Target type: Backend pool 
        - Backend target: [application in Azure Spring Cloud]
        - HTTP settings: add new (select _HTTPS_ and _Pick Host Name From Backend Target_)
    
* Save and create the gateway
* Use the Gateway’s public IP/domain name to access the application from the internet

      
