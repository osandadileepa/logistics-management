# Helm Charts for Services

## How to add your charts

1. There are already 3 main charts. You can add you charts in any of these 3 based on your platform
    * charts/ for RoR applications
    * charts-le-ge/ for LE and GE services
    * charts-springboot for services running on springboot
2. Create a folder to hold your values files. e.g. charts-springboot/shipment-nextgen/
3. Inside your application folder you can add 2 files, values yaml file and a values template file.
    * values files should follow this format values-${env}.yaml. e.g. charts-springboot/shipment-nextgen/values-dev.yaml
    * values template file follows this format values-${env}.yaml.template . e.g. charts-springboot/shipment-nextgen/values-dev.yaml.template
        - values template file holds build time variables like the IMAGE_VERSION of the aplication to be deployed then will be used by our pipeline scripts to generate a new values file with the variables replaced with the expected values.
4. To test your Helm chart you can execute command 
```
helm template ${CHART_FOLDER_PATH} --values ${VALUES_FILE_PATH}
```