# PetitionHub

## Description

PetitionHub est une application web qui permet aux utilisateurs de créer et de signer des pétitions en ligne.
Les utilisateurs peuvent créer des pétitions sur différents sujets, les signer et voir les pétitions les plus populaires, 
l'utilisateur coneecté à la possibilité de trouver tous les utilisaateurs qui ont signé sa pétiton y compris lui même.
Pour les tag sans être connecté on peut avoir toutes les information en reherchant juste le tag concerné, on verra liste
des petitions utilisant le même tag. 

## Réalisateurs 
- GUILAVOGUI David Dogbo
- BETEOU Cherif
- NAWABY Mohammad Fahim

## Fonctionnalités

        ## Ce qui marche (Done)

        -  Authentification
        -  Création de pétitions
        -  Signature de pétitions
        -  Affichage de Top pétitions 
        -  Recherche de pétitions par tag
        -  Recherche par nom de la petition pour afficher tous les signataires de la dite pétiton
        -  Impression d'une petition ( se trouvant les signataires, Date d'impression, Titre de la pétition)
        -  Suppression d'une petition 


        ### Ce qui ne marche pas 

        - Affichage des pétitions signées par un utilisateur 

```markdown
##  Deploiement
    Pour deployer notre application, il vous suffit d'entrez les commandes suivantes:
        Assurez-vous de changer tous nos identifiants google par votre identifiant google.

            mvn install
            mvn package
            mvn appengine:deploy
            
            NB: il est important d'avoir google cloud SDK sinon entrez:
            "gcloud auth login" 
            ET suivez les instructions


## Lien vers l'application

lien vers le site : https://my-project-2425-449914.ew.r.appspot.com


## Lien vers le dépôt uncloud Université de Nantes


```

## License

```markdown
Ce projet est sous licence Nantes Université Master 1 MIAGE. 
Pour plus d'informations, consulter les réalisateurs.
```