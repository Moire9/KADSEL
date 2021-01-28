# KADSEL
The Kotlin ASM Domain Specific Embeddable Language. Yes that's right - embeddable. I am releasing this library under MIT, so you can embed it right in your jar! 

KADSEL is a simple library that allows you to easily select and transform classes with a simple DSL. 

```kotlin
ClassTransformer.registerTransformer {
    clazz("net.minecraft.entity.monster.EntityEnderman") {
        method("teleportTo", "DDD", "Z") {
            instructions {
                insertBefore(first, InsnList().koffee {
                    getstatic(System::class, "out", java.io.PrintStream::class)
                    ldc("TELEPORTING!")
                    invokevirtual(java.io.PrintStream::class, "println", void, java.lang.String::class)
                }.first)
            }
        }
        
        method("entityInit", "Z") {
            instructions {
                insertBefore(first, InsnList().koffee {
                    getstatic(System::class, "out", java.io.PrintStream::class)
                    ldc("Enderman Initialized!")
                    invokevirtual(java.io.PrintStream::class, "println", void, java.lang.String::class)
                }.first)
            }
        }
    }
    method("net.minecraft.client.Minecraft", "displayCrashReport", "Lnet/minecraft/crash/CrashReport;", "V") {
        instructions {
            insertBefore(first, InsnList().koffee {
                getstatic(System::class, "out", java.io.PrintStream::class)
                ldc(":(")
                invokevirtual(java.io.PrintStream::class, "println", void, java.lang.String::class)
            }.first)
        }
    }
}
```

Seen here is usage of [Koffee](https://github.com/videogame-hacker/Koffee), another DSL for generating InsnLists. The two DSLs pair together nicely. 

Currently, it is rather limited in features, really only allowing you to modify instructions cleanly, however more is to come in the future!
