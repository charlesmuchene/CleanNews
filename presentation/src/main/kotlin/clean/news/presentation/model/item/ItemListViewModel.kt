package clean.news.presentation.model.item

import clean.news.app.usecase.item.GetItemsByListType
import clean.news.core.entity.Item
import clean.news.presentation.model.StoreModel
import clean.news.presentation.model.item.ItemListViewModel.Action.*
import clean.news.presentation.model.item.ItemListViewModel.State
import clean.news.presentation.navigation.NavigationFactory
import clean.news.presentation.navigation.NavigationService
import redux.Dispatcher
import redux.Middleware
import redux.Reducer
import redux.Store
import redux.observable.Epic
import redux.observable.EpicMiddleware
import rx.Observable
import rx.Scheduler
import javax.inject.Inject

class ItemListViewModel @Inject constructor(
		private val observeScheduler: Scheduler,
		private val navService: NavigationService,
		private val navFactory: NavigationFactory,
		private val listType: Item.ListType,
		private val getItemsByListType: GetItemsByListType) : StoreModel<State>() {

	init {
		dispatch(Action.GetItems())
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// State

	data class State(
			val items: List<Item>,
			val loading: Boolean)

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Actions

	sealed class Action {
		class GetItems() : Action()
		class ShowItems(val items: List<Item>) : Action()
		class GoToDetail(val item: Item) : Action()
		class GoToUrl(val item: Item) : Action()
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Reducer

	private fun reducer() = Reducer { state: State, action: Any ->
		when (action) {
			is ShowItems -> state.copy(items = action.items)
			else -> state
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Epic

	private fun epic() = Epic { actions: Observable<out Any>, _: Store<State> ->
		actions.ofType(Action.GetItems::class.java)
				.flatMap {
					getItemsByListType.execute(GetItemsByListType.Request(listType))
							.observeOn(observeScheduler)
				}
				.map { Action.ShowItems(it.items) }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Navigation Middleware

	private fun navigationMiddleware() = Middleware { _: Store<State>, action: Any, next: Dispatcher ->
		val result = next.dispatch(action)
		when (action) {
			is GoToUrl -> navService.goTo(navFactory.url(action.item))
			is GoToDetail -> navService.goTo(navFactory.detail(action.item))
		}
		result
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Overrides

	override fun createStore(): Store<State> {
		return Store.create(
				reducer(),
				State(emptyList(), false),
				Middleware.apply(
						navigationMiddleware(),
						EpicMiddleware.create(epic())
				))
	}

}
