# Class diagram

This is a simplified view of the domain model: the JPA entities and how they relate. It shows the important fields, not every one.

![Atlas class diagram](images/class-diagram.png)

## How to read it

- **User** owns everything else. Every entity has a `user_id` (many-to-one), and deleting a user deletes everything it owns. A user has a `Role` (`USER` or `ADMIN`).
- **Pages** (`Chapter`, `Story`, `Memory`, `Person`, `Place`, `Artifact`, `Fragment`) implement the `LinkableEntity` interface. They are the things a mention can link from and to.
- **Artifact** has an **ArtifactType**, a category the user makes up themselves (for example "Clothing").
- **EntityList** holds a list of **EntityListItem**s.
- **TimeCapsule** keeps its content hidden until `unlockDate`. `unlockNotified` makes sure the owner is only told once.
- **Notification** is created by the system (a new devlog, an unlocked time capsule). **Devlog** is written by an admin and read by everyone.
- **Mention** is the interesting one. It stores an *owner* (`ownerType` + `ownerId`) and a *target* (`targetType` + `targetId`), plus the highlighted text (`startIndex`, `endIndex`, `selectedText`). It has **no foreign keys** to the pages, because one mention can point at six different kinds of page and a foreign key can only point at one table. The service layer checks that both pages exist and belong to the user, and the generic DAO removes a page's mentions when the page is deleted.

## Diagram source

The diagram is written in [Mermaid](https://mermaid.js.org/), which GitHub renders directly:

```mermaid
classDiagram
    direction TB

    class User {
        Integer id
        String name
        String email
        String password
        Role role
    }

    class LinkableEntity {
        <<interface>>
        getId() Integer
    }

    class Chapter {
        title
        subtitle
        content
        startDate
        endDate
        visibility
    }
    class Story {
        title
        subtitle
        content
        startDate
        endDate
        visibility
    }
    class Memory {
        title
        subtitle
        content
        date
        visibility
    }
    class Person {
        name
        subtitle
        content
        relation
        visibility
    }
    class Place {
        name
        content
        latitude
        longitude
        city
        country
    }
    class Artifact {
        title
        subtitle
        content
        visibility
    }
    class ArtifactType {
        name
    }
    class Fragment {
        title
        content
    }

    class EntityList {
        title
        subtitle
        visibility
    }
    class EntityListItem {
        text
    }
    class TimeCapsule {
        title
        content
        unlockDate
        unlockNotified
    }

    class Mention {
        ownerType
        ownerId
        startIndex
        endIndex
        selectedText
        targetType
        targetId
    }

    class Notification {
        title
        notificationType
        targetType
        targetId
        read
    }
    class Devlog {
        title
        subtitle
        content
    }
    class GeminiPrompt {
        content
    }

    class Role {
        <<enumeration>>
        USER
        ADMIN
    }
    class TargetType {
        <<enumeration>>
        CHAPTER
        STORY
        MEMORY
        ARTIFACT
        PERSON
        PLACE
    }

    note for User "Owns every class below.<br/>Each one has a user_id (many-to-one).<br/>Deleting a user deletes everything it owns."
    User "1" *-- "*" LinkableEntity : owns
    LinkableEntity <|.. Chapter
    LinkableEntity <|.. Story
    LinkableEntity <|.. Memory
    LinkableEntity <|.. Person
    LinkableEntity <|.. Place
    LinkableEntity <|.. Artifact
    LinkableEntity <|.. Fragment


    Artifact "*" --> "1" ArtifactType
    EntityList "1" *-- "*" EntityListItem
    User ..> Role
    Mention ..> TargetType : owner and target type
    Mention ..> LinkableEntity : points from and to a page (type + id, no foreign key)
```
