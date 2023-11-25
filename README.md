# spread-sheet-reader
sample gradle plugin


### plugin 使用側の build.gradle.kts に設定する

```
plugins {
    id("spread.sheet.reader.output") version "1.0" apply false
}
```

``` kotlin
val outputTask by tasks.registering(SpreadSheetReader::class) {
    spreadSheetId = "1234567890qwertyuiop" // spread sheet id
    spreadSheetRange = "A1:Z100" // data range to retrieve
}
```

### task 実行コマンド

```
$ ./gradlew outputTask
```
