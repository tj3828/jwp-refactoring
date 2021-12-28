package kitchenpos.table.domain;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public final class TableGroupId {
    @Column(name = "table_group_id")
    private final Long id;

    protected TableGroupId() {
        this.id = null;
    }

    private TableGroupId(Long id) {
        this.id = id;
    }

    public static TableGroupId of(Long id) {
        return new TableGroupId(id);
    }

    public Long value() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TableGroupId)) {
            return false;
        }
        TableGroupId id = (TableGroupId) o;
        return Objects.equals(this.id, id.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}