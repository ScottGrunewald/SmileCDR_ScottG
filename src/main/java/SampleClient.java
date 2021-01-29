import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SampleClient {

    public static void main(String[] theArgs) {

/*        // Create a context
        FhirContext ctx = FhirContext.forR4();

        // Create a client
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        // Read a patient with the given ID
        Patient patient = client.read().resource(Patient.class).withId("example").execute();

        // Print the output
        String string = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(string);*/

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

        // We now have the returned XML in the response variable, so we need to print it out.

        List<Bundle.BundleEntryComponent> ourList = new ArrayList(response.getEntry());

        ArrayList<String> ourPatients = new ArrayList();

        for(int i=0;i<ourList.size();i++)
        {
            String string = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(ourList.get(i).getResource());
            StringBuffer patientInfo = new StringBuffer("");

            if(string.contains("given"))
            {
                patientInfo.append(string.substring(string.indexOf("given")+19, string.indexOf("\"",string.indexOf("given")+19)));
            }

            patientInfo.append(" " + string.substring(string.indexOf("family")+10, string.indexOf("\"",string.indexOf("family")+10)));

            if(string.contains("birthDate"))
            {
                patientInfo.append(" " + string.substring(string.indexOf("birthDate") + 13, string.indexOf("\"", string.indexOf("birthDate") + 13)));
            }

            ourPatients.add(patientInfo.toString());
        }

        Collections.sort(ourPatients);

        for(int i=0;i<ourPatients.size();i++)
        {
            System.out.println(ourPatients.get(i));
        }

        System.out.println("Process Completed!");
    }

}
