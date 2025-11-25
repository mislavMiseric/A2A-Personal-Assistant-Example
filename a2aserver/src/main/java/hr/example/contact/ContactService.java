package hr.example.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public Contact createContact(String firstName, String lastName, String email,
                                  String phone, String company, String message) {
        Contact contact = new Contact(firstName, lastName, email);
        contact.setPhone(phone);
        contact.setCompany(company);
        contact.setMessage(message);
        return contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public Page<Contact> list(Pageable pageable) {
        return contactRepository.findAll(pageable);
    }
}

