package clean.news.presentation.model.item

import clean.news.core.entity.Item
import clean.news.presentation.inject.ScreenScope
import clean.news.presentation.model.StoreModel
import clean.news.presentation.model.item.ItemUrlViewModel.Action.*
import clean.news.presentation.model.item.ItemUrlViewModel.State
import clean.news.presentation.navigation.NavigationFactory
import clean.news.presentation.navigation.NavigationFactory.ItemUrlScreen
import clean.news.presentation.navigation.NavigationService
import redux.Dispatcher
import redux.Middleware
import redux.Reducer
import redux.Store
import javax.inject.Inject

@ScreenScope(ItemUrlScreen::class)
class ItemUrlViewModel @Inject constructor(
		private val navService: NavigationService,
		private val navFactory: NavigationFactory,
		val item: Item) : StoreModel<State>() {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// State

	data class State(val item: Item)

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Actions

	sealed class Action {
		class GoBack() : Action()
		class GoToDetails() : Action()
		class Share() : Action()
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Reducer

	private fun reducer() = Reducer { state: State, _: Any -> state }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Middleware

	private fun navigationMiddleware() = Middleware { _: Store<State>, action: Any, next: Dispatcher ->
		val result = next.dispatch(action)
		when (action) {
			is GoBack -> navService.goBack()
			is GoToDetails -> navService.goTo(navFactory.detail(item))
			is Share -> navService.goTo(navFactory.shareDetail(item))
		}
		result
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Middleware

	override fun createStore(): Store<State> {
		return Store.create(
				reducer(),
				State(item),
				Middleware.apply(navigationMiddleware())
		)
	}

}
