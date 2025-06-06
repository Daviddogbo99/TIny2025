package foo;

import java.time.LocalDate;

import java.util.ArrayList;

import java.util.List;

import com.google.api.server.spi.config.Api;

import com.google.api.server.spi.config.ApiMethod;

import com.google.api.server.spi.config.ApiMethod.HttpMethod;

import com.google.api.server.spi.config.ApiNamespace;

import com.google.api.server.spi.config.Named;

import com.google.api.server.spi.config.Nullable;

import com.google.api.server.spi.response.UnauthorizedException;

import com.google.api.server.spi.response.NotFoundException;

import com.google.appengine.api.datastore.DatastoreService;

import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;

import com.google.appengine.api.datastore.FetchOptions;

import com.google.appengine.api.datastore.Query;

import com.google.appengine.api.datastore.PreparedQuery;

import com.google.appengine.api.datastore.Query.CompositeFilterOperator;

import com.google.appengine.api.datastore.Query.FilterOperator;

import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.google.appengine.api.datastore.Key;

import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.HashMap;
import java.util.Map;



import com.google.api.server.spi.config.Authenticator;



// API declaration for petition management

@Api(

    name = "petitionApi",

    version = "v1",

    audiences = "315689940834-7th4d5r8t5g37girtgvs157038o9d0im.apps.googleusercontent.com",

    clientIds = {"315689940834-7th4d5r8t5g37girtgvs157038o9d0im.apps.googleusercontent.com"},

    namespace = @ApiNamespace(

        ownerDomain = "minipetition.example.com",

        ownerName = "minipetition.example.com",

        packagePath = ""

    )

)

public class PetitionEndpoint {

    

    // Method to create a new petition

    @ApiMethod(name = "createPetition", httpMethod = HttpMethod.GET)

    public Entity createPetition(@Named("user") String user, @Named("title") String title, @Named("tag") String tag) throws UnauthorizedException {

        // Checking if user credentials are valid

        if ((user == null) || (user.equals("null"))) throw new UnauthorizedException("Invalid credentials");

        // Datastore setup and preliminary petition existence check

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query checkEmpty = new Query("Petition");

        PreparedQuery checkPq = datastore.prepare(checkEmpty);

        List<Entity> checkResult = checkPq.asList(FetchOptions.Builder.withLimit(1));

        // Checking if the title is already in use

        if (!checkResult.isEmpty()) {

            Query q = new Query("Petition").setFilter(

                    new FilterPredicate("title", FilterOperator.EQUAL, title));

            PreparedQuery pq = datastore.prepare(q);

            List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(2));

            if (!result.isEmpty()) throw new UnauthorizedException("This Petition title already exists.");

        }

        // Creating a new petition entity

        Entity e = new Entity("Petition");

        e.setProperty("title", title);

        e.setProperty("author", user);

        e.setProperty("date", LocalDate.now().toString());

        e.setProperty("tags", tag);

        datastore.put(e);

        return e;

    }

    // Method to sign a petition

    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)

    public Entity signPetition(@Named("user") String user, @Named("title") String title) throws UnauthorizedException {

        // Checking if user credentials are valid

        if ((user == null) || (user.equals("null"))) throw new UnauthorizedException("Invalid credentials");

        // Query to check if the petition exists

        Query q = new Query("Petition").setFilter(

                new FilterPredicate("title", FilterOperator.EQUAL, title));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        PreparedQuery pq = datastore.prepare(q);

        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(5));

        // Verifying if the petition exists

        if (result.isEmpty()) throw new UnauthorizedException("This petition does not exist.");

        // Query to check if the user has already signed this petition

        q = new Query("Petitioner").setFilter(CompositeFilterOperator.and(

                FilterOperator.EQUAL.of("petitioner", user),

                FilterOperator.EQUAL.of("petition", title)));

    /* OR (equivalent)

        q  = new Query("Petitioner").setFilter(new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(

            new FilterPredicate("petitioner", FilterOperator.EQUAL, user), 

            new FilerPredicate("petition", FilterOperator.EQUAL, title)));

    */

        pq = datastore.prepare(q);

        result = pq.asList(FetchOptions.Builder.withLimit(5));

        // Verifying if the user has already signed this petition

        if (!result.isEmpty()) throw new UnauthorizedException("This user has already signed this petition.");

        // Creating a new entry for the petitioner

        Entity e = new Entity("Petitioner");

        e.setProperty("petitioner", user);

        e.setProperty("petition", title);

        e.setProperty("date", LocalDate.now().toString());

        datastore.put(e);

        return e;

    }

    // Method to list the top 100 petitions

    @ApiMethod(name = "topPetitions", httpMethod = HttpMethod.GET)

    public List<Entity> topPetitions(@Nullable @Named("next") String cursorString) {

        // Query to fetch top 100 petitions sorted by date

        Query q = new Query("Petition").addSort("date", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        PreparedQuery pq = datastore.prepare(q);

        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));

        return result;

    }

    // Method to search petitions by tag

    @ApiMethod(name = "taggedPetitions", httpMethod = HttpMethod.GET)

    public List<Entity> taggedPetitions(@Named("tag") String tag, @Nullable @Named("next") String cursorString) {

        // Datastore setup to retrieve entities based on the target tag within their tags

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query allTagsQuery = new Query("Petition");

        PreparedQuery allTagsPQ = datastore.prepare(allTagsQuery);

        List<Entity> allTagsResults = allTagsPQ.asList(FetchOptions.Builder.withLimit(50));

        List<Entity> finalResults = new ArrayList<Entity>();

        // Filtering and adding entities with matching tags to finalResults list

        for (Entity e : allTagsResults) {

            String[] tags = ((String) e.getProperty("tags")).split(",");

            for (String ta : tags) {

                if (ta.trim().equalsIgnoreCase(tag)) {

                    finalResults.add(e);

                    break;

                }

            }

        }

        return finalResults;

    }

// Méthode pour récupérer les pétitions signées pour un utilisateur

@ApiMethod(
    name = "loadSignedPetitions", 
    httpMethod = HttpMethod.GET,
    path="loadSignedPetitions"
)

public List<Entity> loadSignedPetitions(@Named("user") String user)

        throws UnauthorizedException {

    // Checking if user credentials are valid

    if ((user == null) || (user.equals("null"))) 
    throw new UnauthorizedException("Invalid credentials");

    // Query to retrieve petitions signed by the user
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    Query signedPetitionsQuery = new Query("Petitioner")

            .setFilter(new FilterPredicate("petitioner", FilterOperator.EQUAL, user))

            .addSort("date", SortDirection.DESCENDING);

            PreparedQuery pq = datastore.prepare(signedPetitionsQuery);

    List<Entity> signedPetitionsList = pq.asList(FetchOptions.Builder.withLimit(50));

    List<Entity> result = new ArrayList<>();

    // Retrieving details of signed petitions

    for (Entity signedPetition : signedPetitionsList) {

        String petitionTitle = (String) signedPetition.getProperty("petition");

        // Query to retrieve petition details

        Query petitionQuery = new Query("Petition")

                .setFilter(new FilterPredicate("title", FilterOperator.EQUAL, petitionTitle))

                .addSort("date", SortDirection.DESCENDING);

        result.addAll(datastore.prepare(petitionQuery).asList(FetchOptions.Builder.withLimit(50)));

    }

    return result;

}



    // Method to delete a petition

    // Méthode pour supprimer une pétition
    @ApiMethod(
        name = "deletePetition", 
        path = "deletePetition/{title}/{user}",
        httpMethod = ApiMethod.HttpMethod.DELETE
    ) 
    
    // Méthode pour supprimer une pétition

    public Map <String,String> deletePetition(@Named("title") String title, @Named("user") String user) throws UnauthorizedException {

        System.out.println("=== Début de la suppression de la pétition ===");
        System.out.println("Titre reçu : " + title);
        System.out.println("Utilisateur reçu : " + user);
    
        if (user == null || user.equals("null")) {
            System.out.println("Erreur : utilisateur null ou invalide");
            throw new UnauthorizedException("Invalid credentials");
        }
    
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
        // Construire la requête pour trouver la pétition exacte
        Query query = new Query("Petition").setFilter(
            CompositeFilterOperator.and(
                new FilterPredicate("title", FilterOperator.EQUAL, title),
                new FilterPredicate("author", FilterOperator.EQUAL, user)
            )
        );
    
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> result = preparedQuery.asList(FetchOptions.Builder.withLimit(1));
    
        // Affichage debug : nombre de résultats trouvés
        System.out.println("Résultats trouvés dans la base : " + result.size());
    
        if (result.isEmpty()) {
            System.out.println("Aucune pétition trouvée avec ce titre et auteur.");
            throw new UnauthorizedException("Petition not found for title: " + title + " and author: " + user);
        }
    
        // Suppression de la pétition
        Entity petition = result.get(0);
        datastore.delete(petition.getKey());
    
        System.out.println("Pétition supprimée avec succès : " + petition.getProperty("title"));
        System.out.println("=== Fin de la suppression ===");
        Map<String, String> response = new HashMap<>();
    response.put("status", "deleted");
    return response;
    }
    

// Méthode permettant d'afficher la liste des utilisateurs ayant signé une pétition donnée

@ApiMethod(name = "findSignersOfPetition", httpMethod = HttpMethod.GET)
public List<String> signersOfPetition(@Named("petitionTitle") String petitionTitle) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Petitioner")
                        .setFilter(new FilterPredicate("petition", FilterOperator.EQUAL, petitionTitle));
    PreparedQuery preparedQuery = datastore.prepare(query);
    List<String> signers = new ArrayList<>();
    for (Entity petitioner : preparedQuery.asIterable()) {
        String userEmail = (String) petitioner.getProperty("petitioner");
        signers.add(userEmail);
    }
    return signers;
}

}
