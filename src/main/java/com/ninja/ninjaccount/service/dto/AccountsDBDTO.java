package com.ninja.ninjaccount.service.dto;


import javax.persistence.Lob;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the AccountsDB entity.
 */
public class AccountsDBDTO implements Serializable {

    private Long id;

    private String initializationVector;

    @Lob
    private byte[] database;

    private String databaseContentType;
    @NotNull
    @Min(value = 0)
    private Integer nbAccounts;

    private String sum;


    private Long userId;

    private String userLogin;

    @NotNull
    private OperationAccountType operationAccountType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInitializationVector() {
        return initializationVector;
    }

    public void setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
    }

    public byte[] getDatabase() {
        return database;
    }

    public void setDatabase(byte[] database) {
        this.database = database;
    }

    public String getDatabaseContentType() {
        return databaseContentType;
    }

    public void setDatabaseContentType(String databaseContentType) {
        this.databaseContentType = databaseContentType;
    }

    public Integer getNbAccounts() {
        return nbAccounts;
    }

    public void setNbAccounts(Integer nbAccounts) {
        this.nbAccounts = nbAccounts;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public OperationAccountType getOperationAccountType() {
        return operationAccountType;
    }

    public void setOperationAccountType(OperationAccountType operationAccountType) {
        this.operationAccountType = operationAccountType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccountsDBDTO accountsDBDTO = (AccountsDBDTO) o;
        if (accountsDBDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), accountsDBDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AccountsDBDTO{" +
            "id=" + getId() +
            ", initializationVector='" + getInitializationVector() + "'" +
            ", database='" + getDatabase() + "'" +
            ", nbAccounts=" + getNbAccounts() +
            ", sum='" + getSum() + "'" +
            ", user=" + getUserId() +
            ", user='" + getUserLogin() + "'" +
            "}";
    }
}
