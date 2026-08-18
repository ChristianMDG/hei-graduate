# Projet HEI — Gestion des notes, des parcours et des diplômés

## 1. Contexte et objectif

L’objectif du projet est de concevoir une application permettant de gérer les notes des étudiants sur l’ensemble de leur parcours de trois ans à HEI.

L’application doit permettre de gérer les cours, les enseignants, les groupes, les parcours, les examens et les notes. Elle doit également prendre en compte les changements de groupe ou de parcours au cours de la scolarité d’un étudiant.

Le système devra notamment permettre :

- d’affecter des cours à des enseignants chaque année ;
- d’affecter des cours à certains groupes uniquement ;
- de gérer les deux parcours **EL (Écosystème Logiciel)** et **TN (Transformation Numérique)** ;
- d’enregistrer et de publier les notes chaque année ;
- de conserver l’historique des modifications de notes ;
- de générer les relevés de notes annuels au format PDF ;
- d’envoyer les relevés de notes par e-mail de manière asynchrone ;
- de déterminer les étudiants diplômés après leurs trois années de parcours ;
- de générer la liste des diplômés au format Excel ;
- de stocker les fichiers sur AWS S3 ;
- de proposer une interface web simple avec Thymeleaf.

Les données utilisées n’ont pas besoin d’être les données réelles de HEI, mais elles doivent être réalistes et respecter les règles de fonctionnement décrites ci-dessous.

---

# 2. Règles de gestion

## 2.1. Les cours

Chaque cours possède notamment :

- une référence HEI, par exemple `PROG4`, `WEB1`, `SYS1` ;
- un intitulé réel du cours ;
- un nombre de crédits ECTS ;
- éventuellement d’autres informations utiles.

La structure pédagogique doit respecter les règles du système LMD :

- un semestre représente **30 crédits** ;
- une année universitaire représente **60 crédits** ;
- les trois années représentent donc **180 crédits**.

Les cours devront être répartis en fonction des parcours et des années concernées.

---

## 2.2. Les parcours

L'application doit gérer au minimum deux parcours :

- **EL — Écosystème Logiciel**
- **TN — Transformation Numérique**

Tous les cours ne sont pas nécessairement communs aux deux parcours.

Par exemple :

- `PROG4` peut appartenir au parcours EL ;
- `METIER1` peut appartenir au parcours TN.

Un cours appartenant uniquement à un parcours ne doit jamais apparaître dans le relevé d’un étudiant appartenant à l’autre parcours.

Le système doit donc être capable de déterminer précisément les cours qui font partie du parcours d’un étudiant pour une année et un semestre donnés.

---

# 3. Gestion des groupes

Chaque groupe possède :

- un identifiant ;
- une référence, par exemple `K1`, `K2`, `K3`.

Un cours peut être enseigné à plusieurs groupes, mais pas nécessairement à tous les groupes.

Par exemple :

> Le cours `PROG4` peut être enseigné aux groupes K1 et K2, mais pas au groupe K3.

Le système doit donc permettre d'associer une affectation de cours à un ou plusieurs groupes.

---

# 4. Changement de groupe

Un étudiant peut changer de groupe au cours de son parcours.

Par exemple :

- en L1, l'étudiant appartient au groupe K3 ;
- en L2, il passe dans le groupe K1 ;
- pendant le semestre 3, il peut même passer temporairement de K1 à K2 ;
- il peut également changer de parcours, par exemple passer de la partie commune vers EL ou TN.

Le système ne doit jamais supprimer les informations précédentes.

Il faut donc conserver **l'historique des appartenances aux groupes**.

Un étudiant doit pouvoir être associé à plusieurs groupes au cours de son parcours, avec une période de validité pour chaque affectation.

Cela permet de déterminer à quel groupe il appartenait lorsqu'un cours ou un examen lui a été attribué.

---

# 5. Affectation des enseignants

Un cours peut être enseigné par plusieurs enseignants.

Un enseignant peut également enseigner plusieurs cours.

Il faut donc gérer une relation de type plusieurs-à-plusieurs entre les enseignants et les cours.

Cependant, l'affectation doit être contextualisée.

Une affectation doit notamment permettre de déterminer :

- l'année universitaire ;
- le cours ;
- l'enseignant ;
- le ou les groupes concernés ;
- éventuellement le semestre.

Exemple :

> En 2026, le professeur X enseigne `PROG4` aux groupes K1 et K2.

L'année suivante, l'affectation peut être différente.

---

# 6. Utilisateurs et rôles

L'application possède trois types principaux d'utilisateurs :

### Étudiant

Un étudiant peut :

- consulter ses notes ;
- consulter ses relevés de notes ;
- télécharger ses relevés disponibles.

Il ne peut pas modifier ses notes.

### Enseignant

Un enseignant peut :

- consulter les cours qui lui sont affectés ;
- consulter les étudiants concernés ;
- saisir les notes ;
- modifier les notes de ses cours selon les règles prévues.

Un enseignant ne doit jamais pouvoir modifier les notes d'un cours auquel il n'est pas affecté.

Cette contrainte doit être garantie par **Spring Security** et également par les règles métier du backend.

### Administrateur

L'administrateur possède tous les droits.

Il peut notamment :

- gérer les étudiants ;
- gérer les enseignants ;
- gérer les cours ;
- gérer les groupes ;
- gérer les parcours ;
- affecter les cours aux enseignants ;
- gérer les examens ;
- consulter et modifier les notes ;
- consulter les résultats d'une promotion ;
- générer les relevés ;
- générer les listes de diplômés.

---

# 7. Gestion des examens

Un cours peut comporter plusieurs examens.

Chaque examen possède notamment :

- une date ;
- une heure de début ;
- éventuellement une heure de fin ;
- un coefficient ;
- le cours auquel il appartient.

Le coefficient est représenté sous forme de fraction.

Exemples :

- `1/4`
- `1/2`
- `1/3`

La somme des coefficients des examens d'un même cours doit être égale à **1**.

Par exemple :

- Examen 1 : `1/2`
- Examen 2 : `1/4`
- Examen 3 : `1/4`

Total :

`1/2 + 1/4 + 1/4 = 1`

La note finale du cours est calculée à partir des notes obtenues aux différents examens et de leurs coefficients.

---

# 8. Gestion des notes

Une note est associée à :

- un étudiant ;
- un examen ;
- une valeur ;
- une date de saisie ou de modification.

La note finale d'un cours est calculée à partir des résultats des examens correspondant à ce cours.

Une matière est considérée comme validée lorsque la note finale obtenue est **supérieure ou égale à 10/20**.

---

# 9. Historisation des notes

Les notes peuvent être modifiées.

Une modification peut être nécessaire à la suite :

- d'une réclamation d'un étudiant ;
- d'une erreur de transcription ;
- d'une erreur de saisie ;
- d'une correction administrative.

Une note ne doit donc jamais être simplement écrasée sans conserver son ancienne valeur.

Chaque modification doit être historisée avec au minimum :

- l'ancienne note ;
- la nouvelle note ;
- la date de modification ;
- l'utilisateur ayant effectué la modification ;
- la raison de la modification.

Exemple :

> Ancienne note : 8.5  
> Nouvelle note : 10.5  
> Motif : Correction après réclamation de l'étudiant  
> Modifié par : Administrateur  
> Date : 15/07/2026

---

# 10. Relevés de notes

L'application doit permettre de générer les relevés de notes d'un étudiant.

Deux types de relevés doivent être distingués.

### Relevé provisoire

Le relevé peut être incomplet.

Dans ce cas, le document doit clairement indiquer qu'il s'agit d'un :

**RELEVÉ DE NOTES PROVISOIRE**

### Relevé complet

Lorsque toutes les notes nécessaires sont disponibles, l'application doit pouvoir générer un relevé complet indiquant notamment :

- les cours ;
- les notes ;
- les crédits ;
- les crédits obtenus ;
- la moyenne générale annuelle ;
- l'année universitaire ;
- le semestre ou les semestres concernés.

Le système doit également être capable de déterminer si l'année est validée.

---

# 11. Moyenne annuelle

Pour chaque année universitaire, l'application doit être capable de calculer :

- la moyenne générale annuelle ;
- le nombre de crédits obtenus ;
- le nombre de crédits attendus ;
- la liste des cours validés ;
- la liste des cours non validés.

La moyenne doit être calculée conformément aux règles de pondération définies par les crédits des cours.

---

# 12. Diplôme et liste des diplômés

La fonctionnalité la plus importante du projet consiste à déterminer les étudiants pouvant obtenir leur diplôme après trois années d'études.

Un étudiant est considéré comme diplômé lorsqu'il a obtenu une note finale **supérieure ou égale à 10/20 dans tous les cours obligatoires de son parcours sur les trois années**.

Il faut donc prendre en compte :

- les trois années ;
- les six semestres ;
- le parcours de l'étudiant ;
- les changements de groupe ;
- les changements éventuels de parcours ;
- les cours réellement suivis ;
- les notes finales obtenues.

Le simple fait d'appartenir actuellement à un groupe ne suffit donc pas pour déterminer les cours d'un étudiant.

Le système doit reconstituer son parcours académique.

---

# 13. Classement des diplômés

Pour chaque promotion, l'application doit pouvoir générer la liste des diplômés.

Cette liste doit contenir au minimum :

| Rang | STD | Nom | Prénom | Moyenne générale |
|---|---|---|---|---|
| 1 | STD001 | Exemple | Jean | 16.42 |
| 2 | STD024 | Exemple | Marie | 15.87 |
| 3 | STD013 | Exemple | Paul | 15.21 |

La liste doit pouvoir être séparée par parcours :

- diplômés **EL** ;
- diplômés **TN**.

Le classement doit être déterminé à partir de la moyenne générale obtenue sur l'ensemble des trois années.

---

# 14. Génération du fichier Excel

La liste des diplômés doit être générée sous forme de fichier **Excel**.

Le fichier doit contenir notamment :

- la promotion ;
- le parcours ;
- le rang ;
- le numéro étudiant ;
- le nom ;
- le prénom ;
- la moyenne générale.

Le fichier doit être stocké sur **AWS S3**.

Contrairement au relevé de notes, la liste des diplômés ne doit pas être envoyée par e-mail.

Elle doit être directement téléchargeable depuis l'application.

---

# 15. Génération et envoi des relevés

Les relevés de notes doivent être générés au format **PDF**.

Le processus attendu est :

1. calcul des résultats ;
2. génération du PDF ;
3. stockage du PDF sur AWS S3 ;
4. récupération du lien S3 ;
5. préparation de l'e-mail ;
6. envoi de l'e-mail de manière **asynchrone**.

L'envoi de l'e-mail ne doit donc pas bloquer la requête HTTP principale.

---

# 16. Interface Thymeleaf

Une interface web simple doit être réalisée avec **Thymeleaf**.

Le CSS n'est pas une priorité.

L'interface doit notamment permettre :

1. d'afficher la liste des promotions ;
2. de sélectionner une promotion ;
3. d'afficher les informations concernant les diplômés ;
4. de proposer un bouton permettant de télécharger la liste des diplômés au format Excel.

Exemple :

**Promotions**

| Promotion | Parcours | Action |
|---|---|---|
| 2026 | EL | Télécharger les diplômés |
| 2026 | TN | Télécharger les diplômés |
| 2027 | EL | Télécharger les diplômés |
| 2027 | TN | Télécharger les diplômés |

---

# 17. Architecture attendue

L'application devra être conçue avec une architecture propre et maintenable.

Une architecture de type :

**Controller → Service → Repository → Database**

est recommandée.

Les responsabilités doivent être séparées.

### Controller

Responsable de :

- recevoir les requêtes HTTP ;
- valider les entrées ;
- appeler les services ;
- retourner les réponses.

### Service

Responsable de :

- appliquer les règles métier ;
- calculer les notes ;
- déterminer les diplômés ;
- gérer les affectations ;
- orchestrer la génération des documents.

### Repository

Responsable de :

- communiquer avec la base de données ;
- effectuer les opérations de lecture et d'écriture.

### Infrastructure

Responsable notamment de :

- AWS S3 ;
- l'envoi d'e-mails ;
- la génération de PDF ;
- la génération Excel ;
- les traitements asynchrones.

---

# 18. Contraintes de sécurité

Les droits doivent être contrôlés en fonction du rôle.

### Étudiant

Accès uniquement à ses propres données.

Un étudiant ne doit jamais pouvoir consulter les notes d'un autre étudiant simplement en modifiant un identifiant dans l'URL.

### Enseignant

Accès uniquement aux cours qui lui sont affectés.

Un enseignant ne doit pas pouvoir modifier la note d'un étudiant appartenant à un cours qu'il n'enseigne pas.

### Administrateur

Accès complet à l'application.

Les contrôles de sécurité doivent être réalisés à la fois au niveau de l'authentification et des règles métier.

---

# 19. Tests

Le projet doit avoir une couverture de tests d'au moins **80 %**.

Les tests doivent notamment couvrir :

### Tests unitaires

- calcul des notes ;
- calcul des moyennes ;
- calcul des crédits ;
- validation d'une matière ;
- détermination d'un diplômé ;
- classement des diplômés ;
- règles d'affectation.

### Tests d'intégration

- accès aux repositories ;
- calcul des résultats avec la base de données ;
- génération des relevés ;
- génération des fichiers Excel ;
- contrôle des permissions ;
- accès à S3 si possible dans l'environnement de test.

Les règles métier importantes doivent être testées en priorité.

---

# 20. Principales difficultés métier à prendre en compte

Le projet comporte trois difficultés majeures.

### Difficulté 1 — Parcours EL et TN

Les deux parcours ne possèdent pas nécessairement les mêmes cours.

Le système doit empêcher qu'un cours propre au parcours TN apparaisse dans le relevé d'un étudiant EL, et inversement.

### Difficulté 2 — Changement de groupe

Les groupes ne sont pas fixes.

Un étudiant peut passer de K3 à K1, puis à K2.

Les notes obtenues avant et après le changement doivent être conservées.

Le groupe actuel de l'étudiant ne doit donc jamais être utilisé comme seule source de vérité pour reconstruire son parcours.

### Difficulté 3 — Modification des notes

Une note peut être modifiée.

L'ancienne valeur doit rester accessible et le système doit conserver la raison de la modification ainsi que l'utilisateur responsable.

---

# 21. Livrables attendus

Le binôme devra fournir :

- [ ] Le projet fonctionnel ;
- [ ] Le dépôt Git ;
- [ ] Le lien de la préproduction ;
- [ ] Le STD ;
- [ ] Les diagrammes UML ;
- [ ] Le schéma de base de données ;
- [ ] Les tests avec une couverture d'au moins 80 % ;
- [ ] La génération des relevés PDF ;
- [ ] L'envoi asynchrone des relevés par e-mail ;
- [ ] L'utilisation d'AWS S3 ;
- [ ] La génération des listes de diplômés au format Excel ;
- [ ] L'interface Thymeleaf ;
- [ ] Une vidéo de présentation de **3 minutes maximum**.

**Deadline : 20 août à 23 h 59.**

Le projet est réalisé **en binôme**.

---

# 22. Résumé fonctionnel

Le système doit finalement permettre de répondre aux questions suivantes :

> **Quels cours cet étudiant devait-il suivre ?**

> **Dans quel parcours et dans quel groupe était-il lorsqu'il a suivi ce cours ?**

> **Quels enseignants étaient responsables de ce cours ?**

> **Quelles notes l'étudiant a-t-il obtenues ?**

> **Comment sa moyenne annuelle a-t-elle été calculée ?**

> **A-t-il validé les 180 crédits et tous les cours obligatoires de son parcours ?**

> **Peut-il être considéré comme diplômé ?**

> **Quel est son classement parmi les diplômés de sa promotion et de son parcours ?**

Le système doit être capable de répondre à ces questions même lorsqu'un étudiant a changé plusieurs fois de groupe, changé de parcours ou fait modifier une de ses notes.

---

# 23. Technologies attendues

Le projet devra notamment exploiter :

- **Spring Boot** pour le backend ;
- **Spring Security** pour la gestion des rôles et permissions ;
- **Base de données relationnelle** pour les données métier ;
- **Thymeleaf** pour l'interface web ;
- **AWS S3** pour le stockage des fichiers ;
- un système d'envoi d'e-mails ;
- un mécanisme de traitement **asynchrone** ;
- une bibliothèque de génération de **PDF** ;
- une bibliothèque de génération de fichiers **Excel** ;
- des tests unitaires et d'intégration.

L'objectif n'est pas de réaliser une interface graphique complexe, mais de construire une application fiable, sécurisée, testée et capable de gérer correctement les règles métier spécifiques au fonctionnement des parcours, des groupes, des notes et des diplômés.