
package com.tomovwgti.json;

public class AddressJson {
    @SuppressWarnings("unused")
    private static final String TAG = AddressJson.class.getSimpleName();

    Address value;

    public Address getValue() {
        return value;
    }

    public void setValue(Address value) {
        this.value = value;
    }

    public class Address extends MsgBaseJson {
        String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
