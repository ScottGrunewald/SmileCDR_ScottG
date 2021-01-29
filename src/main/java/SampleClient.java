import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.StopWatch;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
//import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.*;

public class SampleClient {

    public static void main(String[] theArgs) {


        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Create StopWatch to time the three cycles
        StopWatch ourTimer = new StopWatch();

        // Create an array of long values to hold the cycle times
        List<Long> ourRunTimes = new ArrayList<>();

        // File where the last names list is held
        String nameListFileName = "C:\\Datafiles\\lastnames.txt";

        // A list of the last names from the file
        List<String> lastNameList = new ArrayList<>();

        // Read the last names from the file
        // "nameListFileName" is defined above and can be changed to any path
        try
        {
            BufferedReader ourReader = new BufferedReader(new FileReader(nameListFileName));
            String currentName;
            while((currentName = ourReader.readLine()) != null)
            {
                lastNameList.add(currentName);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // MASTER LOOP - running the same loop three times for performance testing
        for(int z=1;z<=3;z++)
        {
            // Start message for this iteration
            System.out.println("Iteration " + z);
            // Starting the timer for this iteration
            ourTimer.startTask("loop "+z);
            // Loop through the list of last names and recover the value for each
            for(String lastName : lastNameList) {

                // Instantiate our Bundle - request will be run below
                Bundle response;

                // Run iterations 1 and 2 with normal caching
                if(z==1 || z==2) {
                    // Search for Patient resources
                    response = client
                            .search()
                            .forResource("Patient")
                            .where(Patient.FAMILY.matches().value(lastName))
                            .returnBundle(Bundle.class)
                            .execute();
                }
                // Run iteration 3 with no caching
                else
                {
                    // Create our CacheControlDirective object for the cache control header
                    CacheControlDirective ourCC = new CacheControlDirective();
                    // Set the no-cache value to true so the cache is not accessed
                    ourCC.setNoCache(true);

                    // Search for Patient resources without caching
                    response = client
                            .search()
                            .forResource("Patient")
                            .cacheControl(ourCC)
                            .where(Patient.FAMILY.matches().value(lastName))
                            .returnBundle(Bundle.class)
                            .execute();

                }

                // Break down the Bundle into response list
                List<Bundle.BundleEntryComponent> ourResponses = new ArrayList<>(response.getEntry());

                // Create list of Strings to hold the patient information
                ArrayList<String> ourPatients = new ArrayList<>();

                // Transfer the responses to Strings for parsing
                for (Bundle.BundleEntryComponent ourResponse : ourResponses) {
                    String string = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(ourResponse.getResource());
                    StringBuilder patientInfo = new StringBuilder();

                    // Find the value of given name
                    if (string.contains("given")) {
                        patientInfo.append(string.substring(string.indexOf("given") + 19, string.indexOf("\"", string.indexOf("given") + 19)));
                    }

                    // Find the value of last name
                    patientInfo.append(" " + string.substring(string.indexOf("family") + 10, string.indexOf("\"", string.indexOf("family") + 10)));

                    // Find the value of birthDate
                    if (string.contains("birthDate")) {
                        patientInfo.append(" " + string.substring(string.indexOf("birthDate") + 13, string.indexOf("\"", string.indexOf("birthDate") + 13)));
                    }

                    // Add the new string to the list of strings to be printed
                    ourPatients.add(patientInfo.toString());
                }

                // Sort the list of strings alphabetically (lexical sort - "Z" comes before "a")
                Collections.sort(ourPatients);

                // Print out our list of patient names and birth dates
                for (String ourPatient : ourPatients) {
                    System.out.println(ourPatient);
                }
            }
            // Stop the stopwatch
            ourTimer.endCurrentTask();
            // Record the time for this iteration
            ourRunTimes.add(ourTimer.getMillis());
            // Reset the stopwatch
            ourTimer.restart();
        }
        // Print out the run times for the three iterations
        // First should be long, second should be short, third should be longer than second
        for(int i=0;i<3;i++)
        {
            System.out.println("Run time for iteration " + (i+1) + " is " + ourRunTimes.get(i) + "ms");
        }

        // Process completed!
        System.out.println("Process Completed!");
    }
}
