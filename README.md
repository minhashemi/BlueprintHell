my sketch of project tree

```
BlueprintHell/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── me/
│   │   │       └── minhashemi/
│   │   │           └── yourproject/
│   │   │               ├── model/
│   │   │               │   └── ... (POJOs, data classes, business logic)
│   │   │               ├── view/
│   │   │               │   └── ... (GUI code: Swing, JavaFX, CLI renderers, etc.)
│   │   │               ├── controller/
│   │   │               │   └── ... (Input handling, glue code between model and view)
│   │   │               └── Main.java (entry point)
│   │   └── resources/
│   │       └── ... (config files, JSON data, images, etc.)
│   └── test/
│       └── java/
│           └── com/yourorg/yourproject/
│               └── ... (unit tests)
├── pom.xml
```
