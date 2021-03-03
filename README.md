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

*   Enable client application access by creating an _Azure Active Directory_ application (**AAD**) which enables the Spring Boot app to authenticate itself in order to retrieve the _Column Master Key_ (**CMK**) for enc/dec the data. See [how to register an application](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#register-an-application-with-azure-ad-and-create-a-service-principal)
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