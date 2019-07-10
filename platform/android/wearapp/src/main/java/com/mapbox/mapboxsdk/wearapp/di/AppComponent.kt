import com.example.mapbox.MainActivityModule
import com.example.mapbox.MapboxApp
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Application component refers to application level modules only
 */
@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    MainActivityModule::class
])
interface AppComponent : AndroidInjector<MapboxApp> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MapboxApp>()
}