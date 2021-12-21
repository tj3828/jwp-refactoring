package kitchenpos.application;

import static kitchenpos.common.DomainFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceTest {

	@InjectMocks
	private TableGroupService tableGroupService;

	@Mock
	private OrderDao orderDao;
	@Mock
	private OrderTableDao orderTableDao;
	@Mock
	private TableGroupDao tableGroupDao;

	@Test
	void create() {
		final List<OrderTable> orderTables = Arrays.asList(
			orderTable(1L, null, 4, true),
			orderTable(2L, null, 6, true)
		);
		final TableGroup tableGroup = tableGroup(1L, orderTables);

		given(orderTableDao.findAllByIdIn(any())).willReturn(orderTables);
		given(tableGroupDao.save(any())).willReturn(tableGroup);
		given(orderTableDao.save(any()))
			.willReturn(orderTable(1L, 1L, 4, false))
			.willReturn(orderTable(2L, 1L, 6, false));

		final TableGroup createdTableGroup = tableGroupService.create(tableGroup(null, orderTables));

		assertThat(createdTableGroup.getId()).isNotNull();
		assertThat(createdTableGroup.getOrderTables().size()).isEqualTo(2);
		createdTableGroup.getOrderTables()
			.forEach(orderTable -> {
				assertThat(orderTable.getTableGroupId()).isEqualTo(createdTableGroup.getId());
				assertThat(orderTable.isEmpty()).isFalse();
			});
	}

	@Test
	void create_invalid_order_tables_size() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null, null)));

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null, Collections.emptyList())));

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null,
				Arrays.asList(orderTable(1L, null, 4, true))
			)));
	}

	@Test
	void create_not_found_order_table() {
		given(orderTableDao.findAllByIdIn(any())).willReturn(Arrays.asList(
			orderTable(1L, null, 3, true)
		));

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null, Arrays.asList(
				orderTable(1L, null, 3, true),
				orderTable(2L, null, 2, true)
			))));
	}

	@Test
	void create_not_empty_order_table() {
		final List<OrderTable> orderTables = Arrays.asList(
			orderTable(1L, null, 3, false),
			orderTable(2L, null, 2, true)
		);
		given(orderTableDao.findAllByIdIn(any())).willReturn(orderTables);

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null, orderTables)));
	}

	@Test
	void create_order_table_having_table_group_id_already() {
		final List<OrderTable> orderTables = Arrays.asList(
			orderTable(1L, 1L, 4, true),
			orderTable(2L, null, 4, true)
		);
		given(orderTableDao.findAllByIdIn(any())).willReturn(orderTables);

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.create(tableGroup(null, orderTables)));
	}

	@Test
	void ungroup() {
		final Long tableGroupId = 1L;
		final OrderTable orderTable1 = orderTable(1L, tableGroupId, 4, false);
		final OrderTable orderTable2 = orderTable(2L, tableGroupId, 4, false);

		given(orderTableDao.findAllByTableGroupId(any())).willReturn(Arrays.asList(orderTable1, orderTable2));
		given(orderDao.existsByOrderTableIdInAndOrderStatusIn(any(), anyList())).willReturn(false);

		tableGroupService.ungroup(tableGroupId);

		verify(orderTableDao, times(2)).save(any(OrderTable.class));
		assertThat(orderTable1.getTableGroupId()).isNull();
		assertThat(orderTable2.getTableGroupId()).isNull();
	}

	@Test
	void ungroup_order_table_status_cooking_or_meal() {
		final Long tableGroupId = 1L;
		final OrderTable orderTable1 = orderTable(1L, tableGroupId, 4, false);
		final OrderTable orderTable2 = orderTable(2L, tableGroupId, 4, false);

		given(orderTableDao.findAllByTableGroupId(any())).willReturn(Arrays.asList(orderTable1, orderTable2));
		given(orderDao.existsByOrderTableIdInAndOrderStatusIn(any(), anyList())).willReturn(true);

		assertThatIllegalArgumentException()
			.isThrownBy(() -> tableGroupService.ungroup(tableGroupId));
	}
}
