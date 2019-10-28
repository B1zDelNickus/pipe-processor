# Шаблон конвеер

## Назначение

Назначением шаблона является организация сложной логики обработки, состоящей из множества заменяемых компонентов

## Основные составляющие

1. Контекст (`context`) - объект обработки, выполняет роль модели, все `операции на изменение` производятся относительно контекста, все сведения о `задаче` содержатся в контексте
2. Исполнитель (`handler`) - носитель логики обработки `контекста`, выполняет `операции` на контексте
3. Конвеер (`conveyor`) - обеспечивает корректное применение `исполнителей` к контексту
4. Среда (`environment`) - содержит в себе параметры, умолчания, сервисы, предоставляется `conveyor` для нужд `handler`
5. Маркеры (`markers`) - опциональные маркерные интерфейсы для `context`, `handler`, которые могут модифицировать работу `conveyor`,
то есть `conveyor` "**знает**" маркеры и **должен** на них соответственно реагировать, а `handler` или `context` **могут** быть носителяеми этих маркеров
6. Оболочка исполнителя (`handleWrapper`) - особая разновидность настраиваемого `handler`, для использования в DSL и увязки с маркерами и т.п.

### Дополнительные свойства составляющих

1. Контекст - **может** быть носителем расширений и вспомогательных утилит **общего** характера для упрощения выполнения `операций`, что
может быть использовано `исполнителями`
2. Контекст - **не должен** содержать методов, заменяющих выполнение бизнес-логики исполнителями (нужно четко отличать ситуацию от п.1)
3. Конвеер - **не должен** связывать `context` и свои `handler` со своим экземпляром (чтобы эти объекты могли одновременно использоваться в нескольких экземплярах конвеера)
4. Конвеер - **должен** выступать носителем **среды** - параметров, конфигураций, общих сервисов, умолчаний
5. Конвеер - **должен** передавать **среду** в вызовы `handler`, чтобы они могли использовать ее для самонастройки
6. Конвеер - **должен** работать всегда без генерации исключений (`fail safe`), за регистрацию и обработку исключений и ошибочных ситуаций отвечают **вызывающая сторона** или особые `handler` или `context`
7. Исполнитель - **должен** предоставлять `conveyor` метод для быстрой проверки своей применимости в текущем контексте до непосредственного вызова

> Замечание - всегда будет существовать огромное искушение сделать `context` и `handler` `host-aware` - то есть встраивать ссылку на `conveyor` или `environment` сразу при конструировании, вшивать и вызвать начальные настройки. Это не выглядит жестким нарушением паттерна, но в тоже время им является. `handler` теряют возможность быть `Singleton` и выполняться одновременно в разных контекстах, потому  что скрыто могут как-то использовать свой хост (`conveyor`), находясь в другой среде. `context` во-первых идеологически перестает быть моделью запроса и обработки, не зависящей от среды обработки и при этом еще провоцирует неправильное использование среды через самого себя

## Общая логика вызова **исполнителя** (`handler`) со стороны **конвеера** (`conveyor`)

1. Проверить применимость `handler` в текущем `context` и **среде** - `handler.match(context, environment)`
2. В случае отсуствия применимости `handler` не используется
3. Вызвать `handler` - `handler.execute(context, environment)`
4. В случае возникновения исключения - обработать его по логике, указанной в `environment`, `context` или в умолчаниях конвеера

> Замечание - добавление `environment` выглядит избыточным, кому-то покажется, что теперь сложно сделать простой DSL на `Context.()->Unit`, кто-то решит, что значит `environment` на самом деле часть контекста или должно вшиваться в `handler` при конструировании. Оба этих мнения не верны и про "вшитие" подробно было в предыдущем замечании

## Реализация

### Контекст `context`

1. Может быть любым классом, нет обязательного базового или интерфейса
2. Может быть носителем произвольного числа `marker`, предназначенных для контекстов
3. Может содержать типовые "утилиты" для облегчения работы с моделью для `handler`
4. Может сопровождаться собственным Builder и DSL если конструирование контекста - достаточно сложное
5. Может сопровождаться встроенными или внешними адаптерами для сопряжения с внешними контекстами вызова (например REST или еще в каких-то вариантах)
6. **Не должен** быть носителем сервисов, среды или настроек помимо тех, что связаны с текущим вызовом и его непосредственной обработкой

### Исполнитель `environment`

```kotlin
interface IEnvironment {
   fun has(name:String):Boolean
   // получение свойства среды по имени
   fun get(name:String):String
   // проверка наличия того или иного сервиса
   fun has(serviceClass: Class<*>) : Boolean
   // получение сервиса по типу
   fun <T:Any> get(serviceClass: Class<T>) : T
}
// idioms (приходится писать как расширения, так как в Kotlin нет final на методах интерфейсов)
fun IEnvironment.get(name:String, default:String) = if(has(name)) get(name) else default
fun IEnvironment.get(name:String, default:()->String) = if(has(name)) get(name) else default()
fun IEnvironment.getOrEmpty(name:String) = get(name,"")
fun IEnvironment.getOrNull(name:String):String? = if(has(name)) get(name) else null
fun <T:Any> IEnvironment.get(serviceClass: Class<T>, default: T) = if(has(serviceClass)) get(serviceClass) else default
fun <T:Any> IEnvironment.get(serviceClass: Class<T>, default: ()->T) = if(has(serviceClass)) get(serviceClass) else default()
inline fun <reified T:Any> IEnvironment.getOrNew(serviceClass: Class<T>) = if(has(serviceClass)) get(serviceClass) else T::class.newInstance()
fun <T:Any> IEnvironment.getOrNull(serviceClass: Class<T>):T? = if(has(serviceClass)) get(serviceClass) else null
``` 

1. `get(name)->property` - **может** использовать логику конфигуратора `Spring-Boot` с нормализацией имени и проверкой `System`, `Env` и т.д.
2. `get(clz)->service` - **может** использовать любую известную логику для DI - контейнеров или обертывать подобный контейнер
3. `get(name)->property` - **должен** бросать исключения если какого-то свойства не сконфигурировано
4. `get(clz)->service` - **должен** бросать исключения если какого-то сервиса нет
5. Специализированных маркеров для **среды** не предполагается
6. Строготипизированнх **сред** также не предполагается - унифицированы

### Исполнитель `handler`

Изначально строится для многопоточного конкурентного использования

```kotlin
interface IHandler<T:Any> {
    // проверка применимости
    suspend fun match( context: T, environment: IEnvironment ) : Boolean
    // выполнение
    suspend fun execute( context: T, environment: IEnvironment ) 
}
```

> Замечание - рассматривался вариант, чтобы было `execute (context, env) -> HandlerResult` где результат может еще использовать в какой-то логике, но решили, что это излишне

1. **Не должен** кэшировать сам `environment` или результаты вызовов его методов `has/get` (даже в контексте)
2. **Не должен** кэшировать в себе каких-то данных, полученных в `match` для использования в `execute` (нет гарантий что между этими вызовами не будет вызовов в других контекстах и средах), в случае какой-то острой необходимости или в случае особых жизненных циклов, может кэшировать внутри `context`
3. **Не должен** кэшировать в себе результаты одних `execute` для последующих вызовов, только внутри `context`
4. **Может** выбрасывать исключения (как в `match` так и в `execute`)
5. **Может** носить на себе маркеры, предназначенные для исполнителей

### Конвеер `conveyor`

Общий интерфейс

```
interface IConveyor<T> : MutableList<IHandler<T>> {
   fun set(environment:IEnvironment)
   fun env(): IEnvironment
   suspend fun execute( context : T )
}
//idioms
suspend fun <T> IConveyor<T>.executeResult( context : T ) : T  {
      this.execute(context)
      return context
}
// также возможно нужны идиомы для простых переходов из мира обычных вызовов в корутины
```

То есть конвеер - хранит в себе исполнителей, хранит в себе **среду** и позволяет исполнять себя относительно контекста

### HandleWrapper и маркеры (в общем случае)

1. `HandlerWrapper` сам по себе является `handle`
2. `HandlerWrapper` может обертывать `()->Unit`
3. `HandlerWrapper` может обертывать `T.()->Unit`, `(T,IEnvironment)->Unit` 
4. `HandlerWrapper` может обертывать `match` как `T.()->Boolean`, `(T,IEnvironment)->Unit`
5. Конвеер может в себе держать как врапперы так  и обычные `handler` и не проверяет явно их тип и не использует особой логики

Маркеры это просто некие интерфейсы, которые известны **конвееру**

Маркеры контекста не требуют особого рассмотрения, а вот маркеры исполнителей в связи с наличием `HandlerWrapper` требуют.

Допустим есть маркер `IResultExecuteProvider` который предписывает следующее поведение "если `handler` имеет такой маркер, то надо вызывать не обычный `execute(...):Unit`, а специальный `executeResult(...):HandlerResult`.

Получается, что если мы хотим добавить такую возможность на уровень `HandlerWrapper` и обертывать ламбды вида `T.()->HandlerResult`, то мы должны добавить ему этот интерфейс. Но ведь это будет использоваться только в редких случаях, хотя интефейс определен.

Поэтому наличие маркера определяется на по логике `handler is IResultExecuteProvider`, а по следующей идиоме, которая должна применяться к каждому маркерному интерфейсу:

```kotlin
interface IProcessorMarker
interface IProcessorHandlerMarker: IProcessorMarker
interface IProcessorContextMarker : IProcessorMarker
interface IProcessorCommonMarker: IProcessorHandlerMarker, IProcessorContextMarker 
interface IProcessorHandlerMarkerProvider {
     fun <M:IProcessorHandlerMarker> hasMark(clz:Class<M>):Boolean
}
//...
interface IResultExecuteProvider:IProcessorHandlerMarker // придумали маркер
//...
class HandlerWrapper<T> : IHandle<T>,IProcessorHandlerMarkerProvider,IResultExecuteProvider { // и дали ему определение и на уровне HandlerWrapper
   override fun <M:IProcessorHandlerMarker> hasMark(clz:Class<M>):Boolean { // перекрыли определение маркеров
       if(!this.implements(clz))return false
       if(clz == IResultExecuteProvider::class.java ) return SOME_CUSTOM_LOGIC_FOR_IT()
       return true
   }
}
//...
inline fun <reified T:IProcessorHandlerMarker> IHandler<*>.hasMark():Boolean { //  унифицировали опредление маркера
    if(this is IProcessorHandlerMarkerProvider) return this.hasMark(T::class.java)
    return this is T
}
// тоже полезно
inline fun <reified T:IProcessorHandlerMarker> IHandler<*>.toMarker():T? {
   if( this.hasMark<T>() ) {
       return this as T
   }
   return null
}
```

Тогда не важно враппер перед нами или нет, можно писать в стиле :

```kotlin
handler.toMarker<IResultExecuteProvider>()?.let { /* ... */ }
```
или
```kotlin
if(handler.hasMark<IResultExecuteProvider>()){
   /* ... */
}
```

Более того, раз используется такая вынесенная за обычные `as` , `is` идиома достаточно легко будет доработать логику чтобы выражать маркеры не только как интерфейсы и приведение к ним, а например как аннотации или еще какой-то способ доставки маркерного признака и логики.





